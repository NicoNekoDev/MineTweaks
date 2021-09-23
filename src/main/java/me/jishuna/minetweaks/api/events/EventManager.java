package me.jishuna.minetweaks.api.events;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import me.jishuna.commonlib.events.EventConsumer;
import me.jishuna.minetweaks.MineTweaks;
import me.jishuna.minetweaks.api.module.TweakModule;

public class EventManager {
	private final MineTweaks plugin;

	private Map<Class<? extends Event>, EventConsumer<? extends Event>> listenerMap = new HashMap<>();

	public EventManager(MineTweaks plugin) {
		this.plugin = plugin;
		registerBaseEvents();
	}

	public <T extends Event> boolean registerListener(Class<T> eventClass, Consumer<T> handler) {
		return registerListener(eventClass, handler, EventPriority.NORMAL);
	}

	public <T extends Event> boolean registerListener(Class<T> eventClass, Consumer<T> handler,
			EventPriority priority) {
		if (isListenerRegistered(eventClass))
			return false;

		EventConsumer<? extends Event> consumer = new EventConsumer<>(eventClass, handler);
		consumer.register(this.plugin, priority);

		this.listenerMap.put(eventClass, consumer);
		return true;
	}

	public boolean isListenerRegistered(Class<? extends Event> eventClass) {
		return this.listenerMap.containsKey(eventClass);
	}

	public <T extends Event> void processEvent(T event, Class<T> eventClass) {
		for (TweakModule module : this.plugin.getModuleManager().getModules()) {
			if (module.isEnabled()) {
				module.getEventHandlers(eventClass).forEach(consumer -> consumer.consume(event));
			}
		}
	}

	private void registerBaseEvents() {
		registerListener(PlayerInteractAtEntityEvent.class, event -> processEvent(event, PlayerInteractAtEntityEvent.class), EventPriority.HIGHEST);
		registerListener(PlayerInteractEntityEvent.class, event -> processEvent(event, PlayerInteractEntityEvent.class), EventPriority.HIGHEST);
		registerListener(PlayerInteractEvent.class, event -> processEvent(event, PlayerInteractEvent.class), EventPriority.HIGHEST);
		registerListener(BlockDispenseEvent.class, event -> processEvent(event, BlockDispenseEvent.class), EventPriority.HIGHEST);
		registerListener(EntitySpawnEvent.class, event -> processEvent(event, EntitySpawnEvent.class));
		registerListener(PotionSplashEvent.class, event -> processEvent(event, PotionSplashEvent.class));
	}
}