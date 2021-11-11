package com.dtuchs.jupiter;

import com.dtuchs.domain.User;
import io.qameta.allure.AllureId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.platform.commons.support.AnnotationSupport;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.dtuchs.domain.User.UserType.ORGANIZER;
import static com.dtuchs.domain.User.UserType.PARTICIPANT;
import static org.junit.jupiter.api.extension.ExtensionContext.Namespace.create;

/**
 * Junit Extension для inject-а User в тестовые методы
 * У пользователя может быть указан необходимый тип (ORGANIZER | PARTICIPANT) с помощью аннотации
 *
 * `@UserType(PARTICIPANT)`
 * `@UserType(ORGANIZER)` В случае отсутствия аннотации, по умолчанию будет возвращен пользователь с типом Организатор (ORGANIZER);
 * <p>
 * Включает в себя очереди пользователей и механизм их получения / возврата в очередь
 * <p>
 * ! Работает только в связке с аннотацией AllureId над тестом
 * @see io.qameta.allure.AllureId
 * @see com.dtuchs.domain.User
 * @see com.dtuchs.jupiter.UserType
 */
public class UserExtension implements BeforeEachCallback, AfterTestExecutionCallback, ParameterResolver {

    public static final ExtensionContext.Namespace NAMESPACE = create(UserExtension.class);
    private static final Queue<User>
            ORGANIZER_QUEUE = new ConcurrentLinkedQueue<>(),
            PARTICIPANT_QUEUE = new ConcurrentLinkedQueue<>();

    static {
        ORGANIZER_QUEUE.add(new User().setUserType(ORGANIZER).setLogin("login0").setPassword("re[g"));
        ORGANIZER_QUEUE.add(new User().setUserType(ORGANIZER).setLogin("login1").setPassword(",jrd"));
        ORGANIZER_QUEUE.add(new User().setUserType(ORGANIZER).setLogin("login2").setPassword("ujhp"));
        ORGANIZER_QUEUE.add(new User().setUserType(ORGANIZER).setLogin("login3").setPassword("cdfh"));

        PARTICIPANT_QUEUE.add(new User().setUserType(PARTICIPANT).setLogin("login4").setPassword("vtnc"));
        PARTICIPANT_QUEUE.add(new User().setUserType(PARTICIPANT).setLogin("login5").setPassword("rjrc"));
        PARTICIPANT_QUEUE.add(new User().setUserType(PARTICIPANT).setLogin("login6").setPassword("z,tg"));
        PARTICIPANT_QUEUE.add(new User().setUserType(PARTICIPANT).setLogin("login7").setPassword("abuj"));
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) {
        AllureId allureId = getAllureIdAnnotation(extensionContext);
        List<UserType> userTypes = getUserTypeAnnotationFromParams(extensionContext);
        Map<UserType, User> desiredUsers = new HashMap<>();
        for (UserType userType : userTypes) {
            User user = null;
            while (user == null) {
                if (userType.value() == ORGANIZER) {
                    user = ORGANIZER_QUEUE.poll();
                } else if (userType.value() == PARTICIPANT) {
                    user = PARTICIPANT_QUEUE.poll();
                } else throw new IllegalStateException("Invalid @UserType state");
            }
            Assertions.assertNotNull(user);
            desiredUsers.put(userType, user);
        }
        extensionContext.getStore(NAMESPACE).put(allureId.value(), desiredUsers);
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter().getType().equals(User.class)
                && parameterContext.getParameter().isAnnotationPresent(UserType.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public User resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        AllureId annotation = getAllureIdAnnotation(extensionContext);
        UserType userType = parameterContext.getParameter().getAnnotation(UserType.class);
        Map<UserType, User> usersForTest = (Map<UserType, User>) extensionContext.getStore(NAMESPACE).get(annotation.value());
        return usersForTest.get(userType);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void afterTestExecution(ExtensionContext extensionContext) {
        AllureId annotation = getAllureIdAnnotation(extensionContext);
        Map<UserType, User> usersFromTest = (Map<UserType, User>) extensionContext.getStore(NAMESPACE).get(annotation.value());
        for (User fromTest : usersFromTest.values()) {
            if (fromTest.getUserType() == ORGANIZER) {
                ORGANIZER_QUEUE.add(fromTest);
            } else if (fromTest.getUserType() == PARTICIPANT) {
                PARTICIPANT_QUEUE.add(fromTest);
            } else throw new IllegalStateException("Invalid User state");
        }
    }

    private AllureId getAllureIdAnnotation(ExtensionContext extensionContext) {
        return extensionContext.getRequiredTestMethod().getAnnotation(AllureId.class);
    }

    private List<UserType> getUserTypeAnnotationFromParams(ExtensionContext extensionContext) {
        return Stream.of(extensionContext.getRequiredTestMethod().getParameters())
                .filter(parameter -> parameter.getType().equals(User.class))
                .map(parameter -> AnnotationSupport.findAnnotation(parameter, UserType.class))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}
