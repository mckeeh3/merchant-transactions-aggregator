package io.aggregator.entity;

import io.aggregator.TimeTo;
import io.aggregator.api.TransactionApi;
import org.junit.Test;

import static org.junit.Assert.*;

// This class was initially generated based on the .proto definition by Akka Serverless tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class TransactionTest {

  @Test
  public void exampleTest() {
    // TransactionTestKit testKit = TransactionTestKit.of(Transaction::new);
    // use the testkit to execute a command
    // of events emitted, or a final updated state:
    // EventSourcedResult<SomeResponse> result = testKit.someOperation(SomeRequest);
    // verify the emitted events
    // ExpectedEvent actualEvent = result.getNextEventOfType(ExpectedEvent.class);
    // assertEquals(expectedEvent, actualEvent)
    // verify the final state after applying the events
    // assertEquals(expectedState, testKit.getState());
    // verify the response
    // SomeResponse actualResponse = result.getReply();
    // assertEquals(expectedResponse, actualResponse);
  }

  @Test
  public void createTransactionTest() {
    TransactionTestKit testKit = TransactionTestKit.of(Transaction::new);

    testKit.createTransaction(
        TransactionApi.CreateTransactionCommand
            .newBuilder()
            .setTransactionId("123")
            .setService("456")
            .setAccount("789")
            .setMerchantId("merchant-1")
            .setTransactionAmount(123.45)
            .setTransactionTimestamp(TimeTo.now())
            .build());

    var state = testKit.getState();

    assertEquals(state.getTransactionKey().getTransactionId(), "123");
    assertEquals(state.getTransactionKey().getService(), "456");
    assertEquals(state.getTransactionKey().getAccount(), "789");
    assertEquals("merchant-1", state.getMerchantId());
    assertEquals(123.45, state.getTransactionAmount(), 0.0);
    assertTrue(state.getTransactionTimestamp().getSeconds() > 0);
  }

  @Test
  public void getTransactionTest() {
    TransactionTestKit testKit = TransactionTestKit.of(Transaction::new);

    testKit.createTransaction(
        TransactionApi.CreateTransactionCommand
            .newBuilder()
            .setTransactionId("123")
            .setService("456")
            .setAccount("789")
            .setMerchantId("merchant-1")
            .setTransactionAmount(123.45)
            .setTransactionTimestamp(TimeTo.now())
            .build());

    var response = testKit.getTransaction(
        TransactionApi.GetTransactionRequest
            .newBuilder()
            .setTransactionId("123")
            .setService("456")
            .setAccount("789")
            .build());

    var transaction = response.getReply();

    assertNotNull(transaction);
    assertEquals("123", transaction.getTransactionKey().getTransactionId());
    assertEquals("456", transaction.getTransactionKey().getService());
    assertEquals("789", transaction.getTransactionKey().getAccount());
    assertEquals("merchant-1", transaction.getMerchantId());
    assertEquals(123.45, transaction.getTransactionAmount(), 0.0);
    assertTrue(transaction.getTransactionTimestamp().getSeconds() > 0);
  }
}
