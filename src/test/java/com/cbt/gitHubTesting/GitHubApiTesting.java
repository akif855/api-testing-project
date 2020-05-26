package com.cbt.gitHubTesting;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.internal.common.assertion.AssertionSupport;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.apache.http.util.Asserts;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.*;

import static io.restassured.RestAssured.given;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class GitHubApiTesting {

    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = "https://api.github.com";
    }

    /**
     * Verify organization information
     * 1. Send a get request to /orgs/:org. Request includes :
     * • Path param org with value cucumber
     * 2. Verify status code 200, content type application/json; charset=utf-8
     * 3. Verify value of the login ﬁeld is cucumber
     * 4. Verify value of the name ﬁeld is cucumber
     * 5. Verify value of the id ﬁeld is 320565
     */

    @Test
    public void test1() {

        Map<String,Object> paramsMap = new HashMap<>();
        paramsMap.put("login","cucumber");
        paramsMap.put("name","cucumber");
        paramsMap.put("id","320565");

        Response response = given().accept(ContentType.JSON).
                and().queryParams(paramsMap).
                when().get("/orgs/cucumber");

        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertEquals("application/json; charset=utf-8", response.contentType());
        Assertions.assertTrue(response.body().asString().contains("cucumber"));
        Assertions.assertTrue(response.body().asString().contains("cucumber"));
        Assertions.assertTrue(response.body().asString().contains("320565"));

        response.prettyPrint();
    }

    /**
     * Verify error message
     * 1. Send a get request to /orgs/:org. Request includes :
     * • Header Accept with value application/xml
     * • Path param org with value cucumber
     * 2. Verify status code 415, content type application/json; charset=utf-8
     * 3. Verify response status line include message Unsupported Media Type
     */

    @Test
    public void test2(){

        Response response = given().accept(ContentType.XML).
                when().get("/orgs/cucumber");

        Assertions.assertEquals(415,response.statusCode());
        Assertions.assertEquals("application/json; charset=utf-8", response.contentType());
        Assertions.assertTrue(response.statusLine().contains("Unsupported Media Type"));

        response.prettyPrint();
    }

    /**
     * https://developer.github.com/v3/repos/
     *
     * Number of repositories
     * 1. Send a get request to /orgs/:org. Request includes :
     * • Path param org with value cucumber
     * 2. Grab the value of the ﬁeld public_repos
     * 3. Send a get request to /orgs/:org/repos. Request includes :
     * • Path param org with value cucumber
     * 4. Verify that number of objects in the response  is equal to value from step 2
     */

    @Test
    public void test3(){

        JsonPath jsonPath = given().log().all().
                pathParam("org","cucumber").
                when().get("/orgs/{org}").prettyPeek().jsonPath();

        int publicRepos = jsonPath.getInt(" public_repos");
        System.out.println("publicRepos = " + publicRepos);

        jsonPath = given().log().all().
                pathParam("org", "cucumber").
                queryParam("per_page",100).
                when().get("/orgs/{org}/repos").jsonPath();

                List<Object> repos = jsonPath.getList("id");

                assertThat(publicRepos, is(repos.size()));

    }

    /**
     * Repository id information
     * 1. Send a get request to /orgs/:org/repos. Request includes :
     * • Path param org with value cucumber
     * 2. Verify that id ﬁeld is unique in every in every object in the response
     * 3. Verify that node_id ﬁeld is unique in every in every object in the response
     */

    @Test
    public void test4(){
        JsonPath jsonPath = given().log().all().
                pathParam("org", "cucumber").
                queryParam("per_page", 100).
                when().get("/orgs/{org}/repos").jsonPath();

        List<Object> listReposID = jsonPath.getList("id");
        System.out.println(listReposID.size());

        Set<Object> setReposID = new HashSet<>(listReposID);
        System.out.println(setReposID.size());

        assertThat(listReposID.size(), is(setReposID.size()));

        List<Object> listnodeID = jsonPath.getList("node_id");
        System.out.println(listnodeID.size());

        Set<Object> setNodeID = new HashSet<>(listnodeID);
        System.out.println(setNodeID.size());

        assertThat(listnodeID.size(), is(setNodeID.size()));

    }

    /**
     * Repository owner information
     * 1. Send a get request to /orgs/:org. Request includes :
     * • Path param org with value cucumber
     * 2. Grab the value of the ﬁeld id
     * 3. Send a get request to /orgs/:org/repos. Request includes :
     * • Path param org with value cucumber
     * 4. Verify that value of the id inside the owner object in every response is equal to value from step 2
     */

    @Test
    public void test5(){

        int id = given().pathParam("org","cucumber").
                when().get("/orgs/{org}").jsonPath().getInt("id");

        given().pathParam("org", "cucumber").
                when().get("/orgs/{org}/repos").
                then().body("owner.id", everyItem(equalTo(id)));

    }

    /**
     * Ascending order by full_name sort
     * 1. Send a get request to /orgs/:org/repos. Request includes :
     * • Path param org with value cucumber
     * • Query param sort with value full_name
     * 2. Verify that all repositories are listed in alphabetical order based on the value of the ﬁeld name
     */

    @Test
    public void test6(){

        List<String> listFullName = given().pathParam("org","cucumber").
                queryParam("sort","full_name").
                when().get("/orgs/{org}/repos").jsonPath().getList("full_name");

        System.out.println("listFullName = " + listFullName);

        List<String> copyOfListFullName = new ArrayList<>(listFullName);
        Collections.sort(copyOfListFullName);
        System.out.println("copyOfListFullName = " + copyOfListFullName);


        assertThat(listFullName, is(copyOfListFullName));
    }

    /**
     * Descending order by full_name sort
     * 1. Send a get request to /orgs/:org/repos. Request includes :
     * • Path param org with value cucumber
     * • Query param sort with value full_name
     * • Query param direction with value desc
     * 2. Verify that all repositories are listed in reverser alphabetical order based on the value of the ﬁeld name
     */

    @Test
    public void test7(){

        List<String> listFullName = given().pathParam("org","cucumber").
                queryParam("sort","full_name").
                queryParam("direction","desc").
                when().get("/orgs/{org}/repos").jsonPath().getList("full_name");

        System.out.println("listFullName = " + listFullName);

        List<String> copyOfListFullName = new ArrayList<>(listFullName);
        Collections.sort(copyOfListFullName, Collections.reverseOrder());
        System.out.println("copyOfListFullName = " + copyOfListFullName);

        assertThat(listFullName, is(copyOfListFullName));
    }

    /**
     * Default sort
     * 1. Send a get request to /orgs/:org/repos. Request includes :
     * • Path param org with value cucumber
     * 2. Verify that by default all repositories are listed in descending order based on the value of the ﬁeld created_at
     */

    @Test
    public void test8(){

        List<String> listCreatedDates = given().pathParam("org", "cucumber").
                when().get("/orgs/{org}/repos").jsonPath().getList("created_at");

        List<String> listDatesOnly = new ArrayList<>();

        for (String listCreatedDate : listCreatedDates) {
            listDatesOnly.add(listCreatedDate.split("T")[0]);
        }

        System.out.println("listDatesOnly = " + listDatesOnly);

        List<String>CopyOfListDatesOnly = new ArrayList<>(listCreatedDates);

        Collections.sort(CopyOfListDatesOnly, Collections.reverseOrder());
        System.out.println("CopyOfListDatesOnly = " + CopyOfListDatesOnly);

        assertThat(listDatesOnly, is(CopyOfListDatesOnly));
    }
}

