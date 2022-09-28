package org.opentripplanner.standalone.config.framework.json;

import static java.util.Comparator.comparing;
import static org.opentripplanner.standalone.config.framework.json.OtpVersion.NA;

import com.fasterxml.jackson.databind.JsonNode;
import java.net.URI;
import java.time.Duration;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.DoubleFunction;
import java.util.function.Function;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opentripplanner.util.OtpAppException;

/**
 * This class wrap a {@link JsonNode} and decorate it with type-safe parsing of types used in OTP
 * like enums, date, time, URIs and so on. By wrapping the JsonNode we get consistent parsing rules
 * and the possibility to log unused parameters when the end of parsing a file. Also, the
 * configuration POJOs become cleaner because they do not have any parsing logic in them anymore.
 * <p>
 * This class has 100% test coverage - keep it that way, for the individual configuration POJOs a
 * smoke test is good enough.
 */
public class NodeAdapter {

  private final JsonNode json;

  /**
   * The source is the origin of the configuration. The source can be "DEFAULT", the name of the
   * JSON source files or the "SerializedGraph".
   */
  private final String source;

  /**
   * This class wrap a {@link JsonNode} which might be a child of another node. We keep the path
   * string for logging and debugging purposes
   */
  private final String contextPath;

  /**
   * The map of all children by their name. It is used to be able to produce a list of unused
   * parameters for all children after the parsing is complete.
   */
  private final Map<String, NodeAdapter> childrenByName = new HashMap<>();

  /**
   * A map of all configured parameters for this node. The current JSON document that is parsed
   * may or may not contain the parameter in this map. The map holds information about the
   * parameter and can be used for generating documentation and such.
   */
  private final Map<String, NodeInfo> parameters = new HashMap<>();

  private NodeAdapter(@Nonnull JsonNode node, String source, String contextPath) {
    this.json = node;
    this.source = source;
    this.contextPath = contextPath;
  }

  public NodeAdapter(@Nonnull JsonNode node, String source) {
    this(node, source, null);
  }

  /**
   * Constructor for nested configuration nodes.
   */
  private NodeAdapter(@Nonnull JsonNode node, @Nonnull NodeAdapter parent, String paramName) {
    this(node, parent.source, parent.fullPath(paramName));
    parent.childrenByName.put(paramName, this);
  }

  /** @deprecated TODO DOC Use {@link #asList(String, Function)} */
  @Deprecated
  public List<NodeAdapter> asList() {
    List<NodeAdapter> result = new ArrayList<>();

    // Count elements starting at 1
    int i = 1;
    for (JsonNode node : json) {
      String arrayElementName = "[" + i + "]";
      NodeAdapter child = path(arrayElementName, node);
      result.add(child);
      childrenByName.put(arrayElementName, child);
      ++i;
    }
    return result;
  }

  public String contextPath() {
    return contextPath;
  }

  public String source() {
    return source;
  }

  public boolean isNonEmptyArray() {
    return json.isArray() && json.size() > 0;
  }

  public boolean isObject() {
    return json.isObject() && json.size() > 0;
  }

  public List<NodeInfo> parametersSorted() {
    return parameters.values().stream().sorted(comparing(NodeInfo::name)).toList();
  }

  /** Get a child by name. The child must exist. */
  public NodeAdapter child(String paramName) {
    return childrenByName.get(paramName);
  }

  /** Create new parameter, a builder is returned. */
  public ParameterBuilder of(String paramName) {
    return new ParameterBuilder(this, paramName);
  }

  public boolean isEmpty() {
    return json.isMissingNode();
  }

  /**
   * @deprecated Inline
   */
  @Deprecated
  public NodeAdapter path(String paramName) {
    return of(paramName)
      .withDoc(NA, /*TODO DOC*/"TODO")
      .withExample(/*TODO DOC*/"TODO")
      .withDescription(/*TODO DOC*/"TODO")
      .asObject();
  }

  public <T> List<T> asList(String paramName, Function<NodeAdapter, T> mapper) {
    return of(paramName)
      .withDoc(NA, /*TODO DOC*/"TODO")
      .withDescription(/*TODO DOC*/"TODO")
      .asObjects(mapper);
  }

  public <T> List<T> asList(
    String paramName,
    List<T> defaultValue,
    Function<NodeAdapter, T> mapper
  ) {
    return of(paramName).withDoc(NA, /*TODO DOC*/"TODO").asObjects(defaultValue, mapper);
  }

  /** Delegates to {@link JsonNode#has(String)} */
  public boolean exist(String paramName) {
    return json.has(paramName);
  }

  /**
   * WARNING! Avoid using this method - it bypasses the build in typesafe parsing support. Only
   * use it to provide custom parsing.
   */
  public String asText() {
    return json.asText();
  }

  /** TODO: Inline this */
  public Duration asDuration(String paramName, Duration defaultValue) {
    return of(paramName).withDoc(NA, /*TODO DOC*/"TODO").asDuration(defaultValue);
  }

  /** TODO: Inline this */
  public Duration asDuration(String paramName) {
    return of(paramName).withDoc(NA, /*TODO DOC*/"TODO").asDuration();
  }

  /** TODO: Inline this */
  public Duration asDuration2(String paramName, Duration defaultValue, ChronoUnit unit) {
    return of(paramName).withDoc(NA, /*TODO DOC*/"TODO").asDuration2(defaultValue, unit);
  }

  /** TODO: Inline this */
  public Duration asDuration2(String paramName, ChronoUnit unit) {
    return of(paramName).withDoc(NA, /*TODO DOC*/"TODO").asDuration2(unit);
  }

  /** TODO: Inline this */
  public List<Duration> asDurations(String paramName, List<Duration> defaultValues) {
    return of(paramName).withDoc(NA, /*TODO DOC*/"TODO").asDurations(defaultValues);
  }

  /** TODO: Inline this */
  public Pattern asPattern(String paramName, String defaultValue) {
    return of(paramName).withDoc(NA, /*TODO DOC*/"TODO").asPattern(defaultValue);
  }

  public List<URI> asUris(String paramName) {
    return of(paramName).withDoc(NA, /*TODO DOC*/"TODO").withExample(/*TODO DOC*/"TODO").asUris();
  }

  public URI asUri(String paramName) {
    return of(paramName).withDoc(NA, /*TODO DOC*/"TODO").withExample(/*TODO DOC*/"TODO").asUri();
  }

  public URI asUri(String paramName, String defaultValue) {
    return of(paramName)
      .withDoc(NA, /*TODO DOC*/"TODO")
      .withExample(/*TODO DOC*/"TODO")
      .asUri(defaultValue);
  }

  /** TODO: Inline this */
  public DoubleFunction<Double> asLinearFunction(
    String paramName,
    DoubleFunction<Double> defaultValue
  ) {
    return of(paramName).withDoc(NA, /*TODO DOC*/"TODO").asLinearFunction(defaultValue);
  }

  /** TODO: Inline this */
  public ZoneId asZoneId(String paramName, ZoneId defaultValue) {
    return of(paramName).withDoc(NA, /*TODO DOC*/"TODO").asZoneId(defaultValue);
  }

  // TODO: This method should be inlined

  public Map<String, String> asStringMap(String paramName) {
    return of(paramName)
      .withDoc(NA, /*TODO DOC*/"TODO")
      .withExample(/*TODO DOC*/"TODO")
      .asStringMap();
  }

  /** List all present parameters by name */
  public Iterator<String> parameterNames() {
    return json.fieldNames();
  }

  /**
   * Log unused parameters for the entire configuration file/node tree. Only call this method for the
   * root adapter, once for each config file read.
   */
  public void logAllUnusedParameters(Consumer<String> logger) {
    for (String p : unusedParams()) {
      logger.accept("Unexpected config parameter: '" + p + "' in '" + source + "'");
    }
  }

  /**
   * Be careful when using this method - this bypasses the NodeAdaptor, and we loose
   * track of unused parameters and can not generate documentation for this parameter.
   */
  public JsonNode rawNode(String paramName) {
    return json.path(paramName);
  }

  /** Return the node as a JSON string. */
  public String toJson() {
    return json.toString();
  }

  /** Return the node as a pretty JSON string. */
  public String toPrettyString() {
    return json.toPrettyString();
  }

  /* private methods */

  NodeAdapter path(String paramName, JsonNode node) {
    if (childrenByName.containsKey(paramName)) {
      return childrenByName.get(paramName);
    }
    return new NodeAdapter(node, this, paramName);
  }

  /**
   * This method list all unused parameters(full path), also nested ones. It uses recursion to get
   * child nodes.
   */
  private List<String> unusedParams() {
    List<String> unusedParams = new ArrayList<>();
    Iterator<String> it = json.fieldNames();
    Set<String> parameterNames = parameters.keySet();

    while (it.hasNext()) {
      String fieldName = it.next();
      if (!parameterNames.contains(fieldName)) {
        unusedParams.add(fullPath(fieldName) + ":" + json.get(fieldName));
      }
    }

    for (NodeAdapter c : childrenByName.values()) {
      // Recursive call to get child unused parameters
      unusedParams.addAll(c.unusedParams());
    }
    unusedParams.sort(String::compareTo);
    return unusedParams;
  }

  /**
   * This method validate the given parameter info and save it to the list of parameters.
   * The JSON node is for this parameter is returned, if the node do not exist a "missing node"
   * (node that returns true for isMissingNode) will be returned.
   */
  JsonNode addAndValidateParameterNode(NodeInfo info) {
    addParameterInfo(info);

    if (info.required()) {
      assertRequiredFieldExist(info.name());
    }
    return json.path(info.name());
  }

  public String fullPath(String paramName) {
    return contextPath == null ? paramName : concatPath(contextPath, paramName);
  }

  String concatPath(String a, String b) {
    return a + "." + b;
  }

  /**
   * Return a {@link OtpAppException}. The full path and source is injected into the message if
   * replacing the placeholders {@code "{path}"}, if they exist.
   */
  public OtpAppException createException(String message, String paramName) {
    message += " Parameter: " + fullPath(paramName) + ".";
    message += " Source: " + source + ".";
    return new OtpAppException(message);
  }

  /**
   * Same as {@link #createException(String, String)} with the given cause is appended to the
   * message.
   */
  public OtpAppException createException(String message, String paramName, Exception cause) {
    message += " Details: " + cause.getMessage();
    return createException(message, paramName);
  }

  /* private methods */

  private void addParameterInfo(NodeInfo info) {
    if (parameters.containsKey(info.name())) {
      assertParameterInfoIsEqual(info);
    } else {
      parameters.put(info.name(), info);
    }
  }

  private void assertRequiredFieldExist(String paramName) {
    if (!exist(paramName)) {
      throw requiredFieldMissingException(paramName);
    }
  }

  private void assertParameterInfoIsEqual(NodeInfo info) {
    var other = parameters.get(info.name());
    if (!other.equals(info)) {
      throw new OtpAppException(
        "Two different parameter definitions exist: " + other + " != " + info
      );
    }
  }

  private OtpAppException requiredFieldMissingException(String paramName) {
    return new OtpAppException(
      "Required parameter '" + fullPath(paramName) + "' not found in '" + source + "'."
    );
  }
}
