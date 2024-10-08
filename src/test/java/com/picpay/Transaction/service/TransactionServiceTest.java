package com.picpay.Transaction.service;

import com.picpay.Shared.service.AuthorizationService;
import com.picpay.Shared.service.NotificationService;
import com.picpay.Transaction.dtos.TransactionDTO;
import com.picpay.Transaction.repository.TransactionRepository;
import com.picpay.User.domain.User;
import com.picpay.User.domain.UserType;
import com.picpay.User.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class TransactionServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private TransactionRepository repository;

    @Mock
    private AuthorizationService authService;

    @Mock
    private NotificationService notificationService;

    @Autowired
    @InjectMocks
    private TransactionService transactionService;

    @BeforeEach
    void setup(){
        MockitoAnnotations.initMocks(this);
    }

    @Test
    @DisplayName("Should create transaction successfully when everything is OK")
    void createTransactionSuccess() throws Exception {
        User sender = new User(
                1L,
                "Henrique",
                "Assis",
                "12345678910",
                "henrique@test.com",
                "Senha123#",
                new BigDecimal(10),
                UserType.COMMON);
        User receiver = new User(
                2L,
                "João",
                "Dantas",
                "12345678911",
                "joao@test.com",
                "Senha123#",
                new BigDecimal(10),
                UserType.MERCHANT
        );

        when(userService.findUserById(1L)).thenReturn(sender);
        when(userService.findUserById(2L)).thenReturn(receiver);

        when(authService.authorizeTransaction(any(), any())).thenReturn(true);

        TransactionDTO request = new TransactionDTO(new BigDecimal(10), 1L, 2L);
        transactionService.createTransaction(request);

        verify(repository, times(1)).save(any());

        sender.setBalance(new BigDecimal(0));
        verify(userService, times(1)).saveUser(sender);

        receiver.setBalance(new BigDecimal(20));
        verify(userService, times(1)).saveUser(receiver);

        verify(notificationService, times(1)).sendNotification(sender, "Transação realizada com sucesso.");
        verify(notificationService, times(1)).sendNotification(receiver, "Transação recebida com sucesso.");
    }

    @Test
    @DisplayName("Should throw Exception when transaction is not allowed")
    void createTransactionError() throws Exception {

        User sender = new User(
                1L,
                "Henrique",
                "Assis",
                "12345678910",
                "henrique@test.com",
                "Senha123#",
                new BigDecimal(10),
                UserType.COMMON);
        User receiver = new User(
                2L,
                "João",
                "Dantas",
                "12345678911",
                "joao@test.com",
                "Senha123#",
                new BigDecimal(10),
                UserType.MERCHANT
        );

        when(userService.findUserById(1L)).thenReturn(sender);
        when(userService.findUserById(2L)).thenReturn(receiver);

        when(authService.authorizeTransaction(any(), any())).thenReturn(false);

        Exception thrown = Assertions.assertThrows(Exception.class, ()->{
            TransactionDTO request = new TransactionDTO(new BigDecimal(10), 1L, 2L);
            transactionService.createTransaction(request);
        });

        Assertions.assertEquals("Transação não autorizada", thrown.getMessage());

    }
}
