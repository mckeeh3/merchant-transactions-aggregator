package io.aggregator.api;

import kalix.javasdk.testkit.junit.kalixTestKitResource;
import com.google.protobuf.Empty;
import io.aggregator.Main;
import io.aggregator.entity.SecondEntity;
import org.junit.ClassRule;
import org.junit.Test;

import static java.util.concurrent.TimeUnit.*;

// This class was initially generated based on the .proto definition by Akka Serverless tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

// Example of an integration test calling our service via the Akka Serverless proxy
// Run all test classes ending with "IntegrationTest" using `mvn verify -Pit`
public class SecondIntegrationTest {

  /**
   * The test kit starts both the service container and the Akka Serverless proxy.
   */
  @ClassRule
  public static final kalixTestKitResource testKit = new kalixTestKitResource(Main.createkalix());

  /**
   * Use the generated gRPC client to call the service through the Akka Serverless proxy.
   */
  private final Second client;

  public SecondIntegrationTest() {
    client = testKit.getGrpcClient(Second.class);
  }

  @Test
  public void addTransactionOnNonExistingEntity() throws Exception {
    // TODO: set fields in command, and provide assertions to match replies
    // client.addTransaction(SecondApi.AddTRansactionCommand.newBuilder().build())
    // .toCompletableFuture().get(5, SECONDS);
  }

  @Test
  public void aggregateOnNonExistingEntity() throws Exception {
    // TODO: set fields in command, and provide assertions to match replies
    // client.aggregate(SecondApi.AggregateCommand.newBuilder().build())
    // .toCompletableFuture().get(5, SECONDS);
  }
}
