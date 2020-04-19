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

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.is;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class FeatureTests {

    @LocalServerPort
    private int serverPort;
    private RequestSpecification requestSpecification;

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

        String accessToken = given(requestSpecification)
                .auth().basic("client", "")
                .param("grant_type", "password")
                .param("username", "test-user")
                .param("password", "test-password")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .when()
                .post("/oauth/token")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("access_token", not(is(emptyString())))
                .body("expires_in", is(43199))
                .extract().path("access_token");

        given(requestSpecification)
                .auth().oauth2(accessToken)
                .when()
                .get("/api/me")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("you.name", is("test-user"));
    }
}
