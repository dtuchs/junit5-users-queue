package com.dtuchs;

import io.qameta.allure.AllureId;
import com.dtuchs.domain.User;
import com.dtuchs.jupiter.UserExtension;
import com.dtuchs.jupiter.UserType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.dtuchs.domain.User.UserType.ORGANIZER;
import static com.dtuchs.domain.User.UserType.PARTICIPANT;

@ExtendWith(UserExtension.class)
public class ExampleTest {
    @AllureId("1")
    @Test
    void organizerTest(@UserType(ORGANIZER) User user) {
        Assertions.assertEquals(ORGANIZER, user.getUserType());
    }

    @AllureId("2")
    @Test
    void participantTest(@UserType(PARTICIPANT) User user) {
        Assertions.assertEquals(PARTICIPANT, user.getUserType());
    }
}
