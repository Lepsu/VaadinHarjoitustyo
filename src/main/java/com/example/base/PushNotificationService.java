package com.example.base;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

@Service
public class PushNotificationService {

    // Rekisteröidyt UI-kuuntelijat
    private final List<Consumer<String>> listeners =
            new CopyOnWriteArrayList<>();

    public void register(Consumer<String> listener) {
        listeners.add(listener);
    }

    public void unregister(Consumer<String> listener) {
        listeners.remove(listener);
    }

    // Lähettää push-viestin kaikille rekisteröidyille UI:lle
    public void broadcast(String message) {
        listeners.forEach(listener -> {
            try {
                listener.accept(message);
            } catch (Exception e) {
                // UI saattaa olla suljettu
            }
        });
    }
}