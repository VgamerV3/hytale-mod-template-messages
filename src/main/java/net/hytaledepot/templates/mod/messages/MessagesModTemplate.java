package net.hytaledepot.templates.mod.messages;

import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public final class MessagesModTemplate {
  private final Map<String, AtomicLong> actionCounters = new ConcurrentHashMap<>();
  private final Map<String, String> lastActionBySender = new ConcurrentHashMap<>();
  private final AtomicBoolean demoFlagEnabled = new AtomicBoolean(false);
  private final AtomicLong errorCount = new AtomicLong();
  private final Map<String, String> domainState = new ConcurrentHashMap<>();
  private final Map<String, AtomicLong> numericState = new ConcurrentHashMap<>();

  private volatile Path dataDirectory;

  public void onInitialize(Path dataDirectory) {
    this.dataDirectory = dataDirectory;
    actionCounters.clear();
    lastActionBySender.clear();
    domainState.clear();
    numericState.clear();
  }

  public void onShutdown() {
    actionCounters.clear();
    lastActionBySender.clear();
    domainState.clear();
    numericState.clear();
  }

  public void onHeartbeat(long tick) {
    actionCounters.computeIfAbsent("heartbeat", key -> new AtomicLong()).incrementAndGet();
    if (tick % 90 == 0) {
      actionCounters.computeIfAbsent("milestone", key -> new AtomicLong()).incrementAndGet();
    }
  }

  public String runAction(String sender, String action, long heartbeatTicks) {
    String normalizedSender = String.valueOf(sender == null ? "unknown" : sender);
    String normalizedAction = normalizeAction(action);

    actionCounters.computeIfAbsent(normalizedAction, key -> new AtomicLong()).incrementAndGet();
    lastActionBySender.put(normalizedSender, normalizedAction);

    if ("toggle".equals(normalizedAction)) {
      boolean enabled = toggleFlag(demoFlagEnabled);
      return "[MessagesMod] demoFlag=" + enabled + ", heartbeatTicks=" + heartbeatTicks;
    }

    if ("info".equals(normalizedAction)) {
      return "[MessagesMod] " + diagnostics(normalizedSender, heartbeatTicks);
    }

    String domainResult = handleDomainAction(normalizedSender, normalizedAction, heartbeatTicks);
    if (domainResult != null) {
      return "[MessagesMod] " + domainResult;
    }

    return "[MessagesMod] unknown action='" + normalizedAction + "' (try: info, toggle, sample, broadcast-demo, preview-template, set-template)";
  }

  public String diagnostics(String sender, long heartbeatTicks) {
    String directory = dataDirectory == null ? "unset" : dataDirectory.toString();
    return "sender="
        + sender
        + ", heartbeatTicks="
        + heartbeatTicks
        + ", demoFlag="
        + demoFlagEnabled.get()
        + ", ops="
        + operationCount()
        + ", lastAction="
        + lastActionBySender.getOrDefault(sender, "none")
        + ", errors="
        + errorCount.get()
        + ", domainEntries="
        + domainState.size()
        + ", numericEntries="
        + numericState.size()
        + ", dataDirectory="
        + directory;
  }

  public long operationCount() {
    long total = 0;
    for (AtomicLong value : actionCounters.values()) {
      total += value.get();
    }
    return total;
  }

  public void incrementErrorCount() {
    errorCount.incrementAndGet();
  }

  private String handleDomainAction(String sender, String action, long heartbeatTicks) {
    if ("sample".equals(action) || "broadcast-demo".equals(action)) {
      long count = incrementNumber("messages:broadcast", 1);
      domainState.put("messages:last", "[Server] Maintenance in 10 minutes.");
      return "broadcastQueued=" + count;
    }
    if ("preview-template".equals(action)) {
      String template = domainState.getOrDefault("messages:template", "Welcome {player} to the server.");
      return template.replace("{player}", sender);
    }
    if ("set-template".equals(action)) {
      domainState.put("messages:template", "Hello {player}, your profile sync is complete.");
      return "template updated";
    }
    return null;
  }

  private long incrementNumber(String key, long delta) {
    return numericState.computeIfAbsent(key, item -> new AtomicLong()).addAndGet(delta);
  }

  private long number(String key) {
    return numericState.computeIfAbsent(key, item -> new AtomicLong()).get();
  }

  private void setNumber(String key, long value) {
    numericState.computeIfAbsent(key, item -> new AtomicLong()).set(value);
  }

  private static boolean toggleFlag(AtomicBoolean flag) {
    while (true) {
      boolean current = flag.get();
      boolean next = !current;
      if (flag.compareAndSet(current, next)) {
        return next;
      }
    }
  }

  private static String normalizeAction(String action) {
    String normalized = String.valueOf(action == null ? "" : action).trim().toLowerCase();
    return normalized.isEmpty() ? "sample" : normalized;
  }
}
