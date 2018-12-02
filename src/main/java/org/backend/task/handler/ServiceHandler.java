package org.backend.task.handler;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.backend.task.dto.Account;
import org.backend.task.dto.ErrorMessage;
import org.backend.task.dto.Transfer;
import org.backend.task.dto.event.TransferEvent;
import org.backend.task.service.AccountService;
import org.backend.task.service.ContextService;
import org.backend.task.service.TransferService;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Path("transfer-api")
@Slf4j
@Produces(MediaType.APPLICATION_JSON)
public class ServiceHandler {

    private final ContextService contextService = ContextService.INSTANCE;
    private final AccountService accountService;
    private final TransferService transferService;
    private final Validator validator = new Validator();

    public ServiceHandler() {
        this.accountService = contextService.getAccountService();
        this.transferService = contextService.getTransferService();
    }

    @Path("/accounts")
    @GET
    public Response getAccounts() throws Exception {
        log.debug("get accounts");
        List<Account> accounts = accountService.findAll();
        return Response.ok(accounts, MediaType.APPLICATION_JSON).build();
    }

    @Path("/accounts/{id}")
    @GET
    public Response getAccount(@PathParam("id") String id) throws Exception {
        log.debug("get account by id: {}", id);
        if (validator.isInvalidId(id)) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        Optional<Account> account = accountService.findById(Long.valueOf(id));
        return account.isPresent() ? Response.ok(account.get(), MediaType.APPLICATION_JSON).build()
                : Response.status(Response.Status.NOT_FOUND).build();
    }

    @Path("/accounts")
    @POST
    public Response createAccount(@QueryParam("amount") String amount, @Context UriInfo uriInfo) throws Exception {
        Optional<Account> account = (StringUtils.isEmpty(amount) || !StringUtils.isNumeric(amount)) ?
                accountService.createAccount() : accountService.createAccount(new BigDecimal(amount));

        if (account.isPresent()) {
            UriBuilder builder = uriInfo.getAbsolutePathBuilder();
            builder.path(Long.toString(account.get().getId()));
            return Response.created(builder.build()).build();
        } else {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @Path("/accounts/{id}")
    @DELETE
    public Response closeAccount(@PathParam("id") String id, @Context UriInfo uriInfo) throws Exception {
        log.debug("close account by id: {}", id);
        if (validator.isInvalidId(id)) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        Optional<Account> account = accountService.close(Long.valueOf(id));

        return account.isPresent() ? Response.ok(account.get(), MediaType.APPLICATION_JSON).build()
                : Response.status(Response.Status.NOT_FOUND).build();
    }

    @Path("/accounts/{id}/balance")
    @GET
    public Response balance(@PathParam("id") String id) throws Exception {
        if (validator.isInvalidId(id)) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        return Response.ok(transferService.getBalance(Long.valueOf(id))).build();
    }

    @Path("/accounts/{id}/transfers")
    @GET
    public Response getTransfers(@PathParam("id") String id) throws Exception {
        log.debug("get transfers by id: {}", id);
        if (validator.isInvalidId(id)) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        List<TransferEvent> transfers = transferService.getTransfers(Long.valueOf(id));
        return Response.ok(transfers, MediaType.APPLICATION_JSON).build();
    }

    @Path("/accounts/{senderId}/transfers/{receiverId}")
    @POST
    public Response transfer(@PathParam("senderId") String senderId,
                             @PathParam("receiverId") String receiverId,
                             Transfer transfer) throws Exception {
        if (validator.isInvalidId(senderId) || validator.isInvalidId(receiverId) || validator.isInvalidTransfer(transfer)) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        Optional<ErrorMessage> error = transferService.transfer(Long.valueOf(senderId), Long.valueOf(receiverId), transfer);
        if (error.isPresent()) {
            log.error(error.get().text);
            return Response.status(Response.Status.NOT_ACCEPTABLE).build();
        }
        return Response.ok().build();
    }

    @Path("/accounts/{id}/transfers/date/")
    @GET
    public Response transferHistory(@PathParam("id") String id,
                                    @QueryParam("from") String from,
                                    @QueryParam("to") String to) throws Exception {
        if (validator.isInvalidId(id)) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        Optional<LocalDateTime> fromDate = validator.getLocalDateTime(from);
        Optional<LocalDateTime> toDate = validator.getLocalDateTime(to);
        return Response.ok(transferService.getTransfersByDate(Long.valueOf(id),
                fromDate.orElse(null), toDate.orElse(null))).build();
    }

}
