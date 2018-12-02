package org.backend.task;


import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.backend.task.dto.Account;
import org.backend.task.dto.AccountState;
import org.backend.task.dto.Transfer;
import org.backend.task.handler.Validator;
import org.eclipse.jetty.server.Server;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class RestServiceTest {

    private static Server server;

    private static int port = 8082;
    private static String host;

    @BeforeClass
    public static void setUp() throws Exception {
        server = Launcher.startUp(port);
        System.out.println(server);
        host = "http://localhost:" + port + "/transfer-api";
    }

    @AfterClass
    public static void tearDown() throws Exception {
        server.stop();
    }


    private WebResource getResource(String path) {
        Client client = Client.create();
        return client.resource(host + path);
    }


    @Test
    public void createAccountTest() throws Exception {
        ClientResponse clientResponse = getResource("/accounts/").post(ClientResponse.class);
        assertEquals(clientResponse.getStatus(), Response.Status.CREATED.getStatusCode());
        try {
            getIdFromURI(clientResponse.getLocation());
        } catch (NumberFormatException ex) {
            fail();
        }
    }

    @Test
    public void getAccountTest() throws Exception {
        Long id = getIdFromURI(getResource("/accounts/").post(ClientResponse.class).getLocation());
        Account account = getResource("/accounts/" + id.toString()).get(Account.class);
        assertNotNull(account);
    }

    @Test
    public void createAccountWithMoneyTest() throws Exception {
        BigDecimal amount = BigDecimal.TEN;
        ClientResponse clientResponse = getResource("/accounts/?amount=" + amount).post(ClientResponse.class);
        assertEquals(clientResponse.getStatus(), Response.Status.CREATED.getStatusCode());
        assertEquals(getBalanceById(getIdFromURI(clientResponse.getLocation())), amount);
    }


    @Test
    public void closeAccountTest() throws Exception {
        Long id = getIdFromURI(getResource("/accounts/").post(ClientResponse.class).getLocation());
        Account account = getResource("/accounts/" + id.toString()).delete(Account.class);
        assertEquals(account.getState(), AccountState.CLOSED);
    }

    @Test
    public void badRequestTest() throws Exception {
        Long id = -1L;
        ClientResponse clientResponse = getResource("/accounts/" + id.toString()).get(ClientResponse.class);
        assertEquals(clientResponse.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
        clientResponse = getResource("/accounts/test").get(ClientResponse.class);
        assertEquals(clientResponse.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void accountNotFoundTest() throws Exception {
        Long id = Long.MAX_VALUE;
        ClientResponse clientResponse = getResource("/accounts/" + id.toString()).get(ClientResponse.class);
        assertEquals(clientResponse.getStatus(), Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void getAccountsTest() throws Exception {
        getResource("/accounts/").post();
        List accounts = getResource("/accounts").get(List.class);
        assertFalse(accounts.isEmpty());
    }

    @Test
    public void testTransferSuccess() throws Exception {
        BigDecimal senderAmount = new BigDecimal(100);
        Long senderId = getIdFromURI(getResource("/accounts/?amount=" + senderAmount.toPlainString()).post(ClientResponse.class).getLocation());
        Long receiverId = getIdFromURI(getResource("/accounts/").post(ClientResponse.class).getLocation());
        Transfer transfer = Transfer.builder().amount(BigDecimal.TEN).build();
        ClientResponse response = doTransfer(senderId, receiverId, transfer);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        assertEquals(senderAmount.add(transfer.getAmount().negate()), getBalanceById(senderId));
        assertEquals(transfer.getAmount(), getBalanceById(receiverId));
    }

    private ClientResponse doTransfer(Long from, Long to, Transfer transfer) {
        return getResource("/accounts/" + from + "/transfers/" + to)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .post(ClientResponse.class, transfer);
    }

    @Test
    public void testTransferFail() throws Exception {
        BigDecimal senderAmount = new BigDecimal(1);
        Long senderId = getIdFromURI(getResource("/accounts/?amount=" + senderAmount.toPlainString()).post(ClientResponse.class).getLocation());
        Long receiverId = getIdFromURI(getResource("/accounts/").post(ClientResponse.class).getLocation());
        Transfer transfer = Transfer.builder().amount(BigDecimal.TEN).build();
        ClientResponse response = doTransfer(senderId, receiverId, transfer);
        assertEquals(response.getStatus(), Response.Status.NOT_ACCEPTABLE.getStatusCode());
        assertEquals(senderAmount, getBalanceById(senderId));
        assertEquals(BigDecimal.ZERO, getBalanceById(receiverId));
    }

    @Test
    public void getTransfers() throws Exception {
        BigDecimal senderAmount = new BigDecimal(100);
        Long senderId = getIdFromURI(getResource("/accounts/?amount=" + senderAmount.toPlainString()).post(ClientResponse.class).getLocation());
        Long receiverId = getIdFromURI(getResource("/accounts/").post(ClientResponse.class).getLocation());
        Transfer transfer1 = Transfer.builder().amount(BigDecimal.ONE).build();
        Transfer transfer2 = Transfer.builder().amount(BigDecimal.TEN).build();
        doTransfer(senderId, receiverId, transfer1);
        doTransfer(senderId, receiverId, transfer2);
        String response = getResource("/accounts/" + senderId + "/transfers")
                .type(MediaType.APPLICATION_JSON_TYPE)
                .get(String.class);
        JsonArray array = new JsonParser().parse(response).getAsJsonArray();
        assertEquals(array.get(0).getAsJsonObject().get("amount").getAsBigDecimal(), BigDecimal.ONE);
        assertEquals(array.get(1).getAsJsonObject().get("amount").getAsBigDecimal(), BigDecimal.TEN);
    }

    @Test
    public void getTransferInRangeDate() throws Exception {
        BigDecimal senderAmount = new BigDecimal(100);
        Long senderId = getIdFromURI(getResource("/accounts/?amount=" + senderAmount.toPlainString()).post(ClientResponse.class).getLocation());
        Long receiverId = getIdFromURI(getResource("/accounts/").post(ClientResponse.class).getLocation());
        Transfer transfer1 = Transfer.builder().amount(BigDecimal.ONE).build();
        Transfer transfer2 = Transfer.builder().amount(BigDecimal.TEN).build();
        doTransfer(senderId, receiverId, transfer1);
        Thread.sleep(1000);
        LocalDateTime dateTime = LocalDateTime.now();
        doTransfer(senderId, receiverId, transfer2);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Validator.FORMAT);
        String response = getResource("/accounts/" + senderId + "/transfers/date?from=" + dateTime.format(formatter))
                .type(MediaType.APPLICATION_JSON_TYPE)
                .get(String.class);
        JsonArray array = new JsonParser().parse(response).getAsJsonArray();
        assertEquals(array.size(), 1);
        assertEquals(array.get(0).getAsJsonObject().get("amount").getAsBigDecimal(), BigDecimal.TEN);
    }

    private BigDecimal getBalanceById(Long id) {
        return getResource("/accounts/" + id.toString() + "/balance")
                .type(MediaType.APPLICATION_JSON_TYPE)
                .get(BigDecimal.class);
    }

    private Long getIdFromURI(URI uri) {
        String path = uri.getPath();
        String idStr = path.substring(path.lastIndexOf('/') + 1);
        return Long.parseLong(idStr);
    }

}