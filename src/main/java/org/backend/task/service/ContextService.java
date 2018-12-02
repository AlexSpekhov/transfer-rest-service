package org.backend.task.service;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import lombok.AccessLevel;
import lombok.Getter;
import org.backend.task.service.db.DatabaseService;
import org.backend.task.service.impl.AccountServiceImpl;
import org.backend.task.service.impl.InMemoryDatabaseServiceImpl;
import org.backend.task.service.impl.LockServiceImpl;
import org.backend.task.service.impl.TransferServiceImpl;

@Getter
public class ContextService {

    private final AccountService accountService;
    private final TransferService transferService;
    private final DatabaseService databaseService;
    private final LockService lockService;

    public final static ContextService INSTANCE = new ContextService();


    ContextService() {
        final Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(AccountService.class).to(AccountServiceImpl.class).asEagerSingleton();
                bind(DatabaseService.class).to(InMemoryDatabaseServiceImpl.class).asEagerSingleton();
                bind(TransferService.class).to(TransferServiceImpl.class).asEagerSingleton();
                bind(LockService.class).to(LockServiceImpl.class).asEagerSingleton();
            }
        });
        accountService = injector.getInstance(AccountService.class);
        databaseService = injector.getInstance(DatabaseService.class);
        lockService = injector.getInstance(LockService.class);
        transferService = injector.getInstance(TransferService.class);
    }

}

