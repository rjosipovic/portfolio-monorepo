package com.playground.challenge_manager.challenge.services.impl.challengeservice.chain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.inOrder;

@ExtendWith(MockitoExtension.class)
class AttemptVerifierChainTest {

    @Mock
    private AttemptHandler handler1;
    @Mock
    private AttemptHandler handler2;
    @Mock
    private AttemptHandler handler3;
    private AttemptVerifierChain attemptVerifierChain;

    @BeforeEach
    void setUp() {
        attemptVerifierChain = new AttemptVerifierChain();
        attemptVerifierChain.addHandler(handler1);
        attemptVerifierChain.addHandler(handler2);
        attemptVerifierChain.addHandler(handler3);
    }

    @Test
    void shouldChainHandlersInOrders() {
        //given
        var ctx = new AttemptVerifierContext(null);
        //when
        attemptVerifierChain.handle(ctx);
        //then
        var inOrder = inOrder(handler1, handler2, handler3);
        inOrder.verify(handler1).handle(ctx);
        inOrder.verify(handler2).handle(ctx);
        inOrder.verify(handler3).handle(ctx);
    }
}