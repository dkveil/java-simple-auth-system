package com.example.demo.flow;

import com.example.demo.auth.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.*;

import jakarta.servlet.http.Cookie;

import java.util.Arrays;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AuthFlowMvcTest {

    @Autowired MockMvc      mvc;
    @Autowired ObjectMapper json;

    private Cookie access;
    private Cookie refresh;

    @Test
    void full_happy_path() throws Exception {

        mvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(
                         new RegistrationRequest("john@test.com","secret"))))
           .andExpect(status().isOk());

        MvcResult res = mvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(
                         new LoginRequest("john@test.com","secret"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Zalogowano pomyślnie"))
                .andReturn();

        access  = cookieFromResponse(res,"accessToken");
        refresh = cookieFromResponse(res,"refreshToken");

        mvc.perform(get("/example/me")
                .cookie(access, refresh))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.username").value("john@test.com"))
           .andExpect(jsonPath("$.roles", Matchers.hasItem("ROLE_USER")));

        mvc.perform(get("/example/me"))
           .andExpect(status().isOk())
           .andExpect(content().string("Nie jesteś zalogowany"));
    }

    private Cookie cookieFromResponse(MvcResult res, String name) {
        return Arrays.stream(res.getResponse().getCookies())
                     .filter(c -> name.equals(c.getName()))
                     .findFirst()
                     .orElseThrow(() ->
                             new AssertionError("Cookie " + name + " not found"));
    }
}