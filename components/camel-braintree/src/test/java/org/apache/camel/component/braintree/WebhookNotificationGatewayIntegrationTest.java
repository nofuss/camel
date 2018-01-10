/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.braintree;

import java.util.HashMap;
import java.util.Map;

import com.braintreegateway.BraintreeGateway;
import com.braintreegateway.ConnectedMerchantPayPalStatusChanged;
import com.braintreegateway.ConnectedMerchantStatusTransitioned;
import com.braintreegateway.WebhookNotification;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.braintree.internal.BraintreeApiCollection;
import org.apache.camel.component.braintree.internal.BraintreeConstants;
import org.apache.camel.component.braintree.internal.WebhookNotificationGatewayApiMethod;
import org.junit.Assume;
import org.junit.Test;

public class WebhookNotificationGatewayIntegrationTest extends AbstractBraintreeTestSupport {
    private static final String PATH_PREFIX = BraintreeApiCollection.getCollection().getApiName(WebhookNotificationGatewayApiMethod.class).getName();

    @Test
    public void testParseSubscription() throws Exception {
        Assume.assumeTrue(checkConfigurationProfile(ConfigurationProfile.PUBLIC_PRIVATE_KEYS));
        runParseSubscriptionTest(WebhookNotification.Kind.SUBSCRIPTION_CANCELED);
        runParseSubscriptionTest(WebhookNotification.Kind.SUBSCRIPTION_CHARGED_SUCCESSFULLY);
        runParseSubscriptionTest(WebhookNotification.Kind.SUBSCRIPTION_CHARGED_UNSUCCESSFULLY);
        runParseSubscriptionTest(WebhookNotification.Kind.SUBSCRIPTION_TRIAL_ENDED);
        runParseSubscriptionTest(WebhookNotification.Kind.SUBSCRIPTION_WENT_ACTIVE);
        runParseSubscriptionTest(WebhookNotification.Kind.SUBSCRIPTION_WENT_PAST_DUE);
    }

    private void runParseSubscriptionTest(WebhookNotification.Kind kind) {
        final BraintreeGateway gateway = getGateway();

        Map<String, String> notification = gateway.webhookTesting().sampleNotification(kind, "my_id");

        final WebhookNotification result = sendSampleNotification(notification);

        assertNotNull("parse result", result);
        assertEquals(kind, result.getKind());
        assertEquals("my_id", result.getSubscription().getId());
    }

    @Test
    public void testParseConnectedMerchantStatusTransitioned() throws Exception {
        final BraintreeGateway gateway = getGateway();

        Map<String, String> notification = gateway.webhookTesting().sampleNotification(
                WebhookNotification.Kind.CONNECTED_MERCHANT_STATUS_TRANSITIONED,
                "my_merchant_public_id"
        );

        final WebhookNotification result = sendSampleNotification(notification);

        assertNotNull("parse result", result);
        assertEquals(WebhookNotification.Kind.CONNECTED_MERCHANT_STATUS_TRANSITIONED, result.getKind());
        ConnectedMerchantStatusTransitioned connectedMerchantStatusTransitioned = result.getConnectedMerchantStatusTransitioned();
        assertEquals("my_merchant_public_id", connectedMerchantStatusTransitioned.getMerchantPublicId());
        assertEquals("oauth_application_client_id", connectedMerchantStatusTransitioned.getOAuthApplicationClientId());
        assertEquals("new_status", connectedMerchantStatusTransitioned.getStatus());
    }

    @Test
    public void testParseConnectedMerchantPayPalStatusChanged() throws Exception {
        final BraintreeGateway gateway = getGateway();

        Map<String, String> notification = gateway.webhookTesting().sampleNotification(
                WebhookNotification.Kind.CONNECTED_MERCHANT_PAYPAL_STATUS_CHANGED,
                "my_merchant_public_id"
        );

        final WebhookNotification result = sendSampleNotification(notification);

        assertNotNull("parse result", result);
        assertEquals(WebhookNotification.Kind.CONNECTED_MERCHANT_PAYPAL_STATUS_CHANGED, result.getKind());

        ConnectedMerchantPayPalStatusChanged connectedMerchantPayPalStatusChanged = result.getConnectedMerchantPayPalStatusChanged();
        assertEquals("my_merchant_public_id", connectedMerchantPayPalStatusChanged.getMerchantPublicId());
        assertEquals("oauth_application_client_id", connectedMerchantPayPalStatusChanged.getOAuthApplicationClientId());
        assertEquals("link", connectedMerchantPayPalStatusChanged.getAction());
    }

    private WebhookNotification sendSampleNotification(Map<String, String> notification) {
        final Map<String, Object> headers = new HashMap<>();
        headers.put(BraintreeConstants.PROPERTY_PREFIX + "signature", notification.get("bt_signature"));
        headers.put(BraintreeConstants.PROPERTY_PREFIX + "payload", notification.get("bt_payload"));
        return requestBodyAndHeaders("direct://PARSE", null, headers);
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() {
                // test route for parse
                from("direct://PARSE")
                    .to("braintree://" + PATH_PREFIX + "/parse");
                // test route for verify
                from("direct://VERIFY")
                    .to("braintree://" + PATH_PREFIX + "/verify?inBody=challenge");
            }
        };
    }
}
