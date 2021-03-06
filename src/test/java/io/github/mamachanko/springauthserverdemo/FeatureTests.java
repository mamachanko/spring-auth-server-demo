package io.github.mamachanko.springauthserverdemo;


import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.jwt.crypto.sign.MacSigner;
import org.springframework.security.oauth2.common.ExpiringOAuth2RefreshToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

import java.time.Instant;
import java.util.Date;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.is;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class FeatureTests {

    @LocalServerPort
    private int serverPort;
    private RequestSpecification requestSpecification;

    final String tokenSigningKey = "secret-signing-key";
    final int accessTokenExpiry = 60;
    final int refreshTokenExpiry = 60 * 60;

    @BeforeEach
    void setUp() {
        requestSpecification = new RequestSpecBuilder()
                .setPort(serverPort)
                .addHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Test
    void oauthFlow() {
        given(requestSpecification)
                .when()
                .get("/api/public")
                .then()
                .statusCode(HttpStatus.OK.value());

        given(requestSpecification)
                .when()
                .get("/api/me")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());

        Map<String, String> tokenResponse = given(requestSpecification)
                .auth().basic("client", "")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .param("grant_type", "password")
                .param("username", "test-user")
                .param("password", "test-password")
                .when()
                .post("/oauth/token")
                .then()
                .statusCode(HttpStatus.OK.value())
                .log().all()
                .body("access_token", not(is(emptyOrNullString())))
                .body("refresh_token", not(is(emptyOrNullString())))
                .body("token_type", is("bearer"))
                .body("scope", is("read"))
                .extract().path("$");

        String accessToken = tokenResponse.get("access_token");
        String refreshToken = tokenResponse.get("refresh_token");

        assertThat(convertAccessToken(accessToken, tokenSigningKey).getExpiration())
                .isBeforeOrEqualTo(secondsFromNow(accessTokenExpiry));
        assertThat(convertRefreshToken(refreshToken, tokenSigningKey).getExpiration())
                .isBeforeOrEqualTo(secondsFromNow(refreshTokenExpiry));

        given(requestSpecification)
                .auth().oauth2(accessToken)
                .when()
                .get("/api/me")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("you.name", is("test-user"));

        given(requestSpecification)
                .auth().basic("client", "")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .param("grant_type", "refresh_token")
                .param("refresh_token", refreshToken)
                .when()
                .post("/oauth/token")
                .then()
                .statusCode(HttpStatus.OK.value())
                .log().all()
                .body("access_token", allOf(not(is(emptyOrNullString())), not(is(accessToken))))
                .body("refresh_token", allOf(not(is(emptyOrNullString())), not(is(refreshToken))))
                .body("token_type", is("bearer"))
                .body("scope", is("read"));
    }

    private OAuth2AccessToken convertAccessToken(String accessToken, String secretKey) {
        JwtAccessTokenConverter jwtTokenEnhancer = new JwtAccessTokenConverter();
        jwtTokenEnhancer.setVerifier(new MacSigner(secretKey));
        JwtTokenStore jwtTokenStore = new JwtTokenStore(jwtTokenEnhancer);
        return jwtTokenStore.readAccessToken(accessToken);
    }

    private ExpiringOAuth2RefreshToken convertRefreshToken(String refreshToken, String secretKey) {
        JwtAccessTokenConverter jwtTokenEnhancer = new JwtAccessTokenConverter();
        jwtTokenEnhancer.setVerifier(new MacSigner(secretKey));
        JwtTokenStore jwtTokenStore = new JwtTokenStore(jwtTokenEnhancer);
        return (ExpiringOAuth2RefreshToken) jwtTokenStore.readRefreshToken(refreshToken);
    }

    private Date secondsFromNow(int seconds) {
        return Date.from(Instant.now().plusSeconds(seconds));
    }
}
