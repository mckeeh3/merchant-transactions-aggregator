package io.aggregator.action;

import kalix.javasdk.action.ActionCreationContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.Any;
import com.google.protobuf.Empty;

import io.aggregator.TimeTo;
import io.aggregator.api.SubSecondApi;
import io.aggregator.entity.TransactionEntity;

// This class was initially generated based on the .proto definition by Akka Serverless tooling.
// This is the implementation for the Action Service described in your io/aggregator/action/transaction_to_sub_second_action.proto.proto file.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class TransactionToSubSecondAction extends AbstractTransactionToSubSecondAction {
  private static final Logger log = LoggerFactory.getLogger(TransactionToSubSecondAction.class);

  public TransactionToSubSecondAction(ActionCreationContext creationContext) {
  }

  @Override
  public Effect<Empty> onTransactionCreated(TransactionEntity.TransactionCreated event) {
    log.debug("onTransactionCreated: {}", event);

    var timestamp = event.getTransactionTimestamp();
    var epochSubSecond = TimeTo.fromTimestamp(timestamp).toEpochSubSecond();

    return effects().forward(components().subSecond().addTransaction(
        SubSecondApi.AddTransactionCommand
            .newBuilder()
            .setMerchantId(event.getMerchantId())
            .setServiceCode(event.getTransactionKey().getServiceCode())
            .setAccountFrom(event.getTransactionKey().getAccountFrom())
            .setAccountTo(event.getTransactionKey().getAccountTo())
            .setEpochSubSecond(epochSubSecond)
            .setTransactionId(event.getTransactionKey().getTransactionId())
            .setAmount(event.getTransactionAmount())
            .setTimestamp(timestamp)
            .build()));
  }

  @Override
  public Effect<Empty> ignoreOtherEvents(Any any) {
    return effects().reply(Empty.getDefaultInstance());
  }
}
