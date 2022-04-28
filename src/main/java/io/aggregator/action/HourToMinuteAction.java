package io.aggregator.action;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import kalix.javasdk.action.ActionCreationContext;
import com.google.protobuf.Any;
import com.google.protobuf.Empty;

import io.aggregator.api.MinuteApi;
import io.aggregator.entity.HourEntity;

// This class was initially generated based on the .proto definition by Akka Serverless tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class HourToMinuteAction extends AbstractHourToMinuteAction {

  public HourToMinuteAction(ActionCreationContext creationContext) {
  }

  @Override
  public Effect<Empty> onHourAggregationRequested(HourEntity.HourAggregationRequested event) {
    var results = event.getEpochMinutesList().stream()
        .map(epochMinute -> MinuteApi.AggregateMinuteCommand
            .newBuilder()
            .setMerchantId(event.getMerchantKey().getMerchantId())
            .setServiceCode(event.getMerchantKey().getServiceCode())
            .setAccountFrom(event.getMerchantKey().getAccountFrom())
            .setAccountTo(event.getMerchantKey().getAccountTo())
            .setEpochMinute(epochMinute)
            .setAggregateRequestTimestamp(event.getAggregateRequestTimestamp())
            .setPaymentId(event.getPaymentId())
            .build())
        .map(command -> components().minute().aggregateMinute(command).execute())
        .collect(Collectors.toList());

    var result = CompletableFuture.allOf(results.toArray(new CompletableFuture[results.size()]))
        .thenApply(reply -> effects().reply(Empty.getDefaultInstance()));

    return effects().asyncEffect(result);
  }

  @Override
  public Effect<Empty> ignoreOtherEvents(Any any) {
    return effects().reply(Empty.getDefaultInstance());
  }
}
