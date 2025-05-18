package com.demo.travelcardsystem;

import com.demo.travelcardsystem.service.util.TravelHelper;
import com.demo.travelcardsystem.entity.TravelCard;
import com.demo.travelcardsystem.repository.InMemoryCardTransactionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RechargeTravelCardTests extends IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private InMemoryCardTransactionRepository inMemoryCardTransactionRepository;

    @Autowired
    private TravelHelper travelHelper;

    private static Stream<Arguments> usersGenerator() {
        return Stream.of(
                Arguments.of("1AE101", 30),
                Arguments.of("1AE102", 40)
        );
    }

    @BeforeEach
    void resetRepository() {
        inMemoryCardTransactionRepository.clearTravelCardStore();
    }

    @DisplayName("Service is Up and Running")
    @Test
    void check_if_ping_is_working() throws Exception {
        mockMvc.perform(get("/api/card/ping"))
                .andExpect(status().isOk());
    }

    @DisplayName("User try to register himself successfully")
    @ParameterizedTest
    @MethodSource("usersGenerator")
    void register_user_in_the_system(String cardNumber, double amount) throws Exception {
        TravelCard travelCard = new TravelCard();
        travelCard.setCardNumber(cardNumber);
        travelCard.setBalance(amount);

        mockMvc.perform(post("/api/card/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(travelCard)))
                .andExpect(status().isOk());

        assertThat(inMemoryCardTransactionRepository.findCardByCardNumber(cardNumber)).isEqualTo(travelCard);
    }

    @DisplayName("User try to recharge a invalid card. System throws INVALID_CARD exception")
    @Test
    void register_user_with_invalid_card_number() throws Exception {
        TravelCard travelCard = new TravelCard();
        travelCard.setCardNumber(null);
        travelCard.setBalance(30);

        String errorMsg = mockMvc.perform(post("/api/card/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(travelCard)))
                .andExpect(status().isNotAcceptable())
                .andReturn().getResolvedException().getMessage();

        assertEquals("This card is Invalid. Please use a valid card", errorMsg);
    }

    @DisplayName("Users are able to recharge the card successfully")
    @ParameterizedTest
    @MethodSource("usersGenerator")
    void users_are_able_to_recharge_the_card_successfully(String cardNumber, double amount) throws Exception {
        travelHelper.directUserRegistration(cardNumber, 0);

        mockMvc.perform(post("/api/card/recharge/{rechargeAmount}", amount)
                        .contentType("application/json")
                        .content(cardNumber))
                .andExpect(status().isOk());

        assertEquals(inMemoryCardTransactionRepository.findCardByCardNumber(cardNumber).getBalance(), amount);
    }
}
