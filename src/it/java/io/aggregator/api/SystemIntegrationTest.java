package io.aggregator.api;

import com.google.protobuf.Timestamp;
import com.google.protobuf.util.Timestamps;
import io.aggregator.view.IncidentsByDate;
import io.aggregator.view.IncidentsByDateModel;
import kalix.javasdk.testkit.junit.KalixTestKitResource;
import io.aggregator.Main;
import io.aggregator.TimeTo;
import io.aggregator.entity.TransactionMerchantKey;
import org.junit.ClassRule;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.*;

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

// Example of an integration test calling our service via the Kalix proxy
// Run all test classes ending with "IntegrationTest" using `mvn verify -Pit`
public class SystemIntegrationTest {

  /**
   * The test kit starts both the service container and the Kalix proxy.
   */
  @ClassRule
  public static final KalixTestKitResource testKit =
    new KalixTestKitResource(Main.createKalix());

  /**
   * Use the generated gRPC client to call the service through the Kalix proxy.
   */
  private final Transaction transactionClient;
  private final Merchant merchantClient;
  private final Payment paymentClient;
  private final IncidentsByDate incidentsView;

  public SystemIntegrationTest() {
    transactionClient = testKit.getGrpcClient(Transaction.class);
    merchantClient = testKit.getGrpcClient(Merchant.class);
    paymentClient = testKit.getGrpcClient(Payment.class);
    incidentsView = testKit.getGrpcClient(IncidentsByDate.class);
  }

  @Test
  public void transactionToMerchant() throws Exception {
    System.out.println("TEST START TIME: " + Instant.now().toString());

    transactionClient.paymentPriced(TransactionApi.PaymentPricedCommand
        .newBuilder()
        .setTransactionId("txn-1")
        .setShopId("tesco-chelsea")
        .setEventType("approved")
        .setTimestamp(TimeTo.now())
        .addPricedItem(TransactionApi.PricedItem.newBuilder()
            .setPricedItemAmount("1.01")
            .setServiceCode("SVC1")
            .build())
        .build()).toCompletableFuture().get(10, SECONDS);
    Thread.sleep(20000);
    System.out.println("PAYMENT_PRICED END TIME: " + Instant.now().toString());

    Timestamp from = Timestamps.fromMillis(Instant.now().minusSeconds(1000).toEpochMilli());
    Timestamp to = Timestamps.fromMillis(Instant.now().toEpochMilli());
    var unPayedIncidentsByMerchantAndDateReq = IncidentsByDateModel.IncidentsByDateRequest.newBuilder()
            .setFromDate(from)
            .setToDate(to)
            .setMerchantId("tesco")
            .setPaymentId("0")
            .build();
    var unPayedIncidentsByMerchantAndDateRes = incidentsView.getIncidentsByDate(unPayedIncidentsByMerchantAndDateReq).toCompletableFuture().get(5,SECONDS);
    assertEquals(1,unPayedIncidentsByMerchantAndDateRes.getResultsCount());

    merchantClient.merchantAggregationRequest(MerchantApi.MerchantAggregationRequestCommand
        .newBuilder()
        .setMerchantId("tesco")
        .build()).toCompletableFuture().get(10, SECONDS);
    Thread.sleep(10000);
    System.out.println("MERCHANT_AGGREGATION END TIME: " + Instant.now().toString());

    PaymentApi.PaymentStatusResponse paymentStatusResponse = paymentClient.paymentStatus(PaymentApi.PaymentStatusCommand
        .newBuilder()
        .setMerchantId("tesco")
        .setPaymentId("payment-1")
        .build()).toCompletableFuture().get(10, SECONDS);
    Thread.sleep(10000);
    System.out.println("PAYMENT_STATUS END TIME: " + Instant.now().toString());

    assertNotNull(paymentStatusResponse);
    assertEquals("tesco", paymentStatusResponse.getMerchantId());
    assertEquals("payment-1", paymentStatusResponse.getPaymentId());
    assertEquals(1, paymentStatusResponse.getMoneyMovementsCount());
    TransactionMerchantKey.MoneyMovement moneyMovement = paymentStatusResponse.getMoneyMovements(0);
    assertEquals("JPMC", moneyMovement.getAccountFrom());
    assertEquals("MERCHANT-TESCO", moneyMovement.getAccountTo());
    assertEquals("1.01", moneyMovement.getAmount());

    //check
    unPayedIncidentsByMerchantAndDateRes = incidentsView.getIncidentsByDate(unPayedIncidentsByMerchantAndDateReq).toCompletableFuture().get(5,SECONDS);
    assertEquals(0,unPayedIncidentsByMerchantAndDateRes.getResultsCount());

    var payedIncidentsByMerchantAndDateReq = unPayedIncidentsByMerchantAndDateReq
            .toBuilder()
            .setPaymentId("payment-1")
            .build();
    var payedIncidentsByMerchantAndDateRes = incidentsView.getIncidentsByDate(payedIncidentsByMerchantAndDateReq).toCompletableFuture().get(5,SECONDS);
    assertEquals(1,payedIncidentsByMerchantAndDateRes.getResultsCount());
    assertEquals("payment-1", payedIncidentsByMerchantAndDateRes.getResults(0).getPaymentId());




  }

  @Test
  public void transactionToMerchantBulk() throws Exception {
    System.out.println("TEST START TIME: " + Instant.now().toString());

    Random random = new Random();
    List<String> merchants = List.of(
        "tesco",
        "amazon",
        "netflix",
        "walmart",
        "starbucks"
    );
    int[] numberOfEventsByMerchant = new int[merchants.size()];
    int[] amountByMerchant = new int[merchants.size()];

    for (int i = 1; i <= 300; i++) {
      int merchantIndex = random.nextInt(merchants.size());
      numberOfEventsByMerchant[merchantIndex]++;
      amountByMerchant[merchantIndex] += i;
      String merchant = merchants.get(merchantIndex);
      transactionClient.paymentPriced(TransactionApi.PaymentPricedCommand
          .newBuilder()
          .setTransactionId("txn-" + i)
          .setShopId(merchant + "-" + random.nextInt(10000))
          .setEventType("approved")
          .setTimestamp(TimeTo.now())
          .addPricedItem(TransactionApi.PricedItem.newBuilder()
              .setPricedItemAmount(BigDecimal.valueOf(i).add(new BigDecimal("0.111")).toString())
              .setServiceCode("SVC1")
              .build())
          .addPricedItem(TransactionApi.PricedItem.newBuilder()
              .setPricedItemAmount(BigDecimal.valueOf(i).add(new BigDecimal("0.222")).toString())
              .setServiceCode("SVC2")
              .build())
          .addPricedItem(TransactionApi.PricedItem.newBuilder()
              .setPricedItemAmount(BigDecimal.valueOf(i).add(new BigDecimal("0.333")).toString())
              .setServiceCode("SVC3")
              .build())
          .addPricedItem(TransactionApi.PricedItem.newBuilder()
              .setPricedItemAmount(BigDecimal.valueOf(i).add(new BigDecimal("0.444")).toString())
              .setServiceCode("SVC4")
              .build())
          .build());
    }
    System.out.println("PAYMENT_PRICED END TIME: " + Instant.now().toString());
    Thread.sleep(30000);

    for (String merchant : merchants) {
      merchantClient.merchantAggregationRequest(MerchantApi.MerchantAggregationRequestCommand
          .newBuilder()
          .setMerchantId(merchant)
          .build());
    }
    System.out.println("MERCHANT_AGGREGATION END TIME: " + Instant.now().toString());
    Thread.sleep(20000);

    for (int i = 0; i < merchants.size(); i++) {
      String merchant = merchants.get(i);
      PaymentApi.PaymentStatusResponse paymentStatusResponse = paymentClient.paymentStatus(PaymentApi.PaymentStatusCommand
          .newBuilder()
          .setMerchantId(merchant)
          .setPaymentId("payment-1")
          .build()).toCompletableFuture().get(10, SECONDS);
      assertNotNull(paymentStatusResponse);
      assertEquals(merchant, paymentStatusResponse.getMerchantId());
      assertEquals("payment-1", paymentStatusResponse.getPaymentId());
      assertEquals(4, paymentStatusResponse.getMoneyMovementsCount());
      String merchantAccount = "MERCHANT-" + merchant.toUpperCase();
      Optional<TransactionMerchantKey.MoneyMovement> optionalSvc1 = findMoneyMovement(paymentStatusResponse, "JPMC", merchantAccount);
      assertTrue(optionalSvc1.isPresent());
      assertEquals(BigDecimal.valueOf(amountByMerchant[i]).add(BigDecimal.valueOf(0.111).multiply(new BigDecimal(numberOfEventsByMerchant[i]))).toString(), optionalSvc1.get().getAmount());
      Optional<TransactionMerchantKey.MoneyMovement> optionalSvc2 = findMoneyMovement(paymentStatusResponse, merchantAccount, "JPMC");
      assertTrue(optionalSvc2.isPresent());
      assertEquals(BigDecimal.valueOf(amountByMerchant[i]).add(BigDecimal.valueOf(0.222).multiply(new BigDecimal(numberOfEventsByMerchant[i]))).toString(), optionalSvc2.get().getAmount());
      Optional<TransactionMerchantKey.MoneyMovement> optionalSvc3 = findMoneyMovement(paymentStatusResponse, merchantAccount, "TAX");
      assertTrue(optionalSvc3.isPresent());
      assertEquals(BigDecimal.valueOf(amountByMerchant[i]).add(BigDecimal.valueOf(0.333).multiply(new BigDecimal(numberOfEventsByMerchant[i]))).toString(), optionalSvc3.get().getAmount());
      Optional<TransactionMerchantKey.MoneyMovement> optionalSvc4 = findMoneyMovement(paymentStatusResponse, merchantAccount, "CARD-SCHEME");
      assertTrue(optionalSvc4.isPresent());
      assertEquals(BigDecimal.valueOf(amountByMerchant[i]).add(BigDecimal.valueOf(0.444).multiply(new BigDecimal(numberOfEventsByMerchant[i]))).toString(), optionalSvc4.get().getAmount());
    }
  }

  private Optional<TransactionMerchantKey.MoneyMovement> findMoneyMovement(PaymentApi.PaymentStatusResponse paymentStatusResponse, String from, String to) {
    return paymentStatusResponse.getMoneyMovementsList().stream()
        .filter(moneyMovement -> moneyMovement.getAccountFrom().equals(from) && moneyMovement.getAccountTo().equals(to))
        .findFirst();
  }
}
