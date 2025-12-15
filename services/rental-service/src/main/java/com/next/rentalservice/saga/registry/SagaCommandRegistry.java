package com.next.rentalservice.saga.registry;

import com.next.rentalservice.saga.command.SagaCommand;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class SagaCommandRegistry {

    private final Map<String, SagaCommand> commands = new ConcurrentHashMap<>();
    private volatile List<SagaCommand> orderedCommands;

    public SagaCommandRegistry(List<SagaCommand> springCommands) {
        springCommands.forEach(this::register);
        log.info("Initialized SagaCommandRegistry with {} commands", commands.size());
    }

    public void register(SagaCommand command) {
        commands.put(command.getName(), command);
        orderedCommands = null;
        log.info("Registered saga command: {}", command.getName());
    }

    public void unregister(String commandName) {
        commands.remove(commandName);
        orderedCommands = null;
        log.info("Unregistered saga command: {}", commandName);
    }

    public Optional<SagaCommand> getCommand(String name) {
        return Optional.ofNullable(commands.get(name));
    }

    public List<SagaCommand> getOrderedCommands() {
        if (orderedCommands == null) {
            synchronized (this) {
                if (orderedCommands == null) {
                    orderedCommands = commands.values().stream()
                            .sorted(Comparator.comparingInt(SagaCommand::getOrder))
                            .toList();
                }
            }
        }
        return orderedCommands;
    }

    public List<SagaCommand> getReversedCommands() {
        List<SagaCommand> ordered = new ArrayList<>(getOrderedCommands());
        Collections.reverse(ordered);
        return ordered;
    }

    public boolean hasCommand(String name) {
        return commands.containsKey(name);
    }

    public Set<String> getCommandNames() {
        return Collections.unmodifiableSet(commands.keySet());
    }
}
