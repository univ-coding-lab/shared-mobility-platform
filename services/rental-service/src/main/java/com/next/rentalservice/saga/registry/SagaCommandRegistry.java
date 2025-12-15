package com.next.rentalservice.saga.registry;

import com.next.rentalservice.saga.command.SagaCommand;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for managing saga commands.
 * Supports dynamic command registration and ordered retrieval.
 */
@Slf4j
@Component
public class SagaCommandRegistry {

    private final Map<String, SagaCommand> commands = new ConcurrentHashMap<>();
    private volatile List<SagaCommand> orderedCommands;

    /**
     * Constructor that auto-registers Spring-managed commands.
     */
    public SagaCommandRegistry(List<SagaCommand> springCommands) {
        springCommands.forEach(this::register);
        log.info("Initialized SagaCommandRegistry with {} commands", commands.size());
    }

    /**
     * Register a command.
     */
    public void register(SagaCommand command) {
        commands.put(command.getName(), command);
        orderedCommands = null; // Invalidate cache
        log.info("Registered saga command: {}", command.getName());
    }

    /**
     * Unregister a command.
     */
    public void unregister(String commandName) {
        commands.remove(commandName);
        orderedCommands = null; // Invalidate cache
        log.info("Unregistered saga command: {}", commandName);
    }

    /**
     * Get a command by name.
     */
    public Optional<SagaCommand> getCommand(String name) {
        return Optional.ofNullable(commands.get(name));
    }

    /**
     * Get all commands in execution order.
     */
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

    /**
     * Get commands in reverse order for compensation.
     */
    public List<SagaCommand> getReversedCommands() {
        List<SagaCommand> ordered = new ArrayList<>(getOrderedCommands());
        Collections.reverse(ordered);
        return ordered;
    }

    /**
     * Check if a command is registered.
     */
    public boolean hasCommand(String name) {
        return commands.containsKey(name);
    }

    /**
     * Get all registered command names.
     */
    public Set<String> getCommandNames() {
        return Collections.unmodifiableSet(commands.keySet());
    }
}
