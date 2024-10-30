// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements. See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership. The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License. You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied. See the License for the
// specific language governing permissions and limitations
// under the License.

package org.apache.doris.flink.rest;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.doris.flink.cfg.ConfigurationOptions;
import org.apache.doris.flink.cfg.DorisOptions;
import org.apache.doris.flink.cfg.DorisReadOptions;
import org.apache.doris.flink.exception.ConnectedFailedException;
import org.apache.doris.flink.exception.DorisException;
import org.apache.doris.flink.exception.IllegalArgumentException;
import org.apache.doris.flink.exception.ShouldNeverHappenException;
import org.apache.doris.flink.rest.models.Backend;
import org.apache.doris.flink.rest.models.BackendRow;
import org.apache.doris.flink.rest.models.BackendV2;
import org.apache.doris.flink.rest.models.QueryPlan;
import org.apache.doris.flink.rest.models.Schema;
import org.apache.doris.flink.rest.models.Tablet;
import org.apache.flink.shaded.guava18.com.google.common.annotations.VisibleForTesting;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.zyx.flink.common.utils.JsonUtils;

import static org.apache.doris.flink.cfg.ConfigurationOptions.DORIS_TABLET_SIZE;
import static org.apache.doris.flink.cfg.ConfigurationOptions.DORIS_TABLET_SIZE_DEFAULT;
import static org.apache.doris.flink.cfg.ConfigurationOptions.DORIS_TABLET_SIZE_MIN;
import static org.apache.doris.flink.util.ErrorMessages.CONNECT_FAILED_MESSAGE;
import static org.apache.doris.flink.util.ErrorMessages.ILLEGAL_ARGUMENT_MESSAGE;
import static org.apache.doris.flink.util.ErrorMessages.SHOULD_NOT_HAPPEN_MESSAGE;

/**
 * Service for communicate with Doris FE.
 */
public class RestService implements Serializable {
    public static final int REST_RESPONSE_STATUS_OK = 200;
    public static final int REST_RESPONSE_CODE_OK = 0;
    private static final String REST_RESPONSE_BE_ROWS_KEY = "rows";
    private static final String API_PREFIX = "/api";
    private static final String SCHEMA = "_schema";
    private static final String QUERY_PLAN = "_query_plan";
    @Deprecated
    private static final String BACKENDS = "/rest/v1/system?path=//backends";
    private static final String BACKENDS_V2 = "/api/backends?is_alive=true";
    private static final String FE_LOGIN = "/rest/v1/login";

    /**
     * send request to Doris FE and get response json string.
     *
     * @param options configuration of request
     * @param request {@link HttpRequestBase} real request
     * @param logger  {@link Logger}
     * @return Doris FE response in json string
     * @throws ConnectedFailedException throw when cannot connect to Doris FE
     */
    private static String send(final DorisOptions options,
                               final DorisReadOptions readOptions, final HttpRequestBase request,
                               final Logger logger) throws ConnectedFailedException {
        final int connectTimeout = readOptions
                .getRequestConnectTimeoutMs() == null
                ? ConfigurationOptions.DORIS_REQUEST_CONNECT_TIMEOUT_MS_DEFAULT
                : readOptions.getRequestConnectTimeoutMs();
        final int socketTimeout = readOptions.getRequestReadTimeoutMs() == null
                ? ConfigurationOptions.DORIS_REQUEST_READ_TIMEOUT_MS_DEFAULT
                : readOptions.getRequestReadTimeoutMs();
        final int retries = readOptions.getRequestRetries() == null
                ? ConfigurationOptions.DORIS_REQUEST_RETRIES_DEFAULT
                : readOptions.getRequestRetries();
        logger.trace(
                "connect timeout set to '{}'. socket timeout set to '{}'. retries set to '{}'.",
                connectTimeout, socketTimeout, retries);

        final RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(connectTimeout)
                .setSocketTimeout(socketTimeout).build();

        request.setConfig(requestConfig);
        logger.info("Send request to Doris FE '{}' with user '{}'.",
                request.getURI(), options.getUsername());
        IOException ex = null;
        final int statusCode = -1;

        for (int attempt = 0; attempt < retries; attempt++) {
            logger.debug("Attempt {} to request {}.", attempt,
                    request.getURI());
            try {
                final String response;
                if (request instanceof HttpGet) {
                    response = getConnectionGet(request.getURI().toString(),
                            options.getUsername(), options.getPassword(),
                            logger);
                } else {
                    response = getConnectionPost(request, options.getUsername(),
                            options.getPassword(), logger);
                }
                if (response == null) {
                    logger.warn(
                            "Failed to get response from Doris FE {}, http code is {}",
                            request.getURI(), statusCode);
                    continue;
                }
                logger.trace(
                        "Success get response from Doris FE: {}, response is: {}.",
                        request.getURI(), response);
                // Handle the problem of inconsistent data format returned by
                // http v1 and v2
                final ObjectMapper mapper = new ObjectMapper();
                final Map map = mapper.readValue(response, Map.class);
                if (map.containsKey("code") && map.containsKey("msg")) {
                    final Object data = map.get("data");
                    return mapper.writeValueAsString(data);
                } else {
                    return response;
                }
            } catch (final IOException e) {
                ex = e;
                logger.warn(CONNECT_FAILED_MESSAGE, request.getURI(), e);
            }
        }

        logger.error(CONNECT_FAILED_MESSAGE, request.getURI(), ex);
        throw new ConnectedFailedException(request.getURI().toString(),
                statusCode, ex);
    }

    private static String getConnectionPost(final HttpRequestBase request,
                                            final String user, final String passwd, final Logger logger)
            throws IOException {
        final URL url = new URL(request.getURI().toString());
        final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setInstanceFollowRedirects(false);
        conn.setRequestMethod(request.getMethod());
        final String authEncoding = Base64.getEncoder()
                .encodeToString(String.format("%s:%s", user, passwd)
                        .getBytes(StandardCharsets.UTF_8));
        conn.setRequestProperty("Authorization", "Basic " + authEncoding);
        final InputStream content = ((HttpPost) request).getEntity()
                .getContent();
        final String res = IOUtils.toString(content);
        conn.setDoOutput(true);
        conn.setDoInput(true);
        final PrintWriter out = new PrintWriter(conn.getOutputStream());
        // send request params
        out.print(res);
        // flush
        out.flush();
        // read response
        return parseResponse(conn, logger);
    }

    private static String getConnectionGet(final String request,
                                           final String user, final String passwd, final Logger logger)
            throws IOException {
        final URL realUrl = new URL(request);
        // open connection
        final HttpURLConnection connection = (HttpURLConnection) realUrl
                .openConnection();
        final String authEncoding = Base64.getEncoder()
                .encodeToString(String.format("%s:%s", user, passwd)
                        .getBytes(StandardCharsets.UTF_8));
        connection.setRequestProperty("Authorization", "Basic " + authEncoding);

        connection.connect();
        return parseResponse(connection, logger);
    }

    private static String parseResponse(final HttpURLConnection connection,
                                        final Logger logger) throws IOException {
        if (connection.getResponseCode() != HttpStatus.SC_OK) {
            logger.warn(
                    "Failed to get response from Doris  {}, http code is {}",
                    connection.getURL(), connection.getResponseCode());
            throw new IOException("Failed to get response from Doris");
        }
        String result = "";
        final BufferedReader in = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), "utf-8"));
        String line;
        while ((line = in.readLine()) != null) {
            result += line;
        }
        if (in != null) {
            in.close();
        }
        return result;
    }

    /**
     * parse table identifier to array.
     *
     * @param tableIdentifier table identifier string
     * @param logger          {@link Logger}
     * @return first element is db name, second element is table name
     * @throws IllegalArgumentException table identifier is illegal
     */
    @VisibleForTesting
    static String[] parseIdentifier(final String tableIdentifier,
                                    final Logger logger) throws IllegalArgumentException {
        logger.trace("Parse identifier '{}'.", tableIdentifier);
        if (StringUtils.isEmpty(tableIdentifier)) {
            logger.error(ILLEGAL_ARGUMENT_MESSAGE, "table.identifier",
                    tableIdentifier);
            throw new IllegalArgumentException("table.identifier",
                    tableIdentifier);
        }
        final String[] identifier = tableIdentifier.split("\\.");
        if (identifier.length != 2) {
            logger.error(ILLEGAL_ARGUMENT_MESSAGE, "table.identifier",
                    tableIdentifier);
            throw new IllegalArgumentException("table.identifier",
                    tableIdentifier);
        }
        return identifier;
    }

    /**
     * choice a Doris FE node to request.
     *
     * @param feNodes Doris FE node list, separate be comma
     * @param logger  slf4j logger
     * @return the chosen one Doris FE node
     * @throws IllegalArgumentException fe nodes is illegal
     */
    @VisibleForTesting
    static String randomEndpoint(final String feNodes, final Logger logger)
            throws IllegalArgumentException {
        logger.trace("Parse fenodes '{}'.", feNodes);
        if (StringUtils.isEmpty(feNodes)) {
            logger.error(ILLEGAL_ARGUMENT_MESSAGE, "fenodes", feNodes);
            throw new IllegalArgumentException("fenodes", feNodes);
        }
        final List<String> nodes = Arrays.asList(feNodes.split(","));
        Collections.shuffle(nodes);
        return nodes.get(0).trim();
    }

    /**
     * choice a Doris FE node to request.
     *
     * @param feNodes Doris FE node list, separate be comma
     * @param logger  slf4j logger
     * @return the array of Doris FE nodes
     * @throws IllegalArgumentException fe nodes is illegal
     */
    @VisibleForTesting
    static List<String> allEndpoints(final String feNodes, final Logger logger)
            throws IllegalArgumentException {
        logger.trace("Parse fenodes '{}'.", feNodes);
        if (StringUtils.isEmpty(feNodes)) {
            logger.error(ILLEGAL_ARGUMENT_MESSAGE, "fenodes", feNodes);
            throw new IllegalArgumentException("fenodes", feNodes);
        }
        final List<String> nodes = Arrays.stream(feNodes.split(","))
                .map(String::trim).collect(Collectors.toList());
        Collections.shuffle(nodes);
        return nodes;
    }

    /**
     * choice a Doris BE node to request.
     *
     * @param options configuration of request
     * @param logger  slf4j logger
     * @return the chosen one Doris BE node
     * @throws IllegalArgumentException BE nodes is illegal
     */
    @VisibleForTesting
    public static String randomBackend(final DorisOptions options,
                                       final DorisReadOptions readOptions, final Logger logger)
            throws DorisException, IOException {
        final List<BackendRow> backends = getBackends(options, readOptions,
                logger);
        logger.trace("Parse beNodes '{}'.", backends);
        if (backends == null || backends.isEmpty()) {
            logger.error(ILLEGAL_ARGUMENT_MESSAGE, "beNodes", backends);
            throw new IllegalArgumentException("beNodes",
                    String.valueOf(backends));
        }
        Collections.shuffle(backends);
        final BackendRow backend = backends.get(0);
        return backend.getIP() + ":" + backend.getHttpPort();
    }

    /**
     * get Doris BE nodes to request.
     *
     * @param options configuration of request
     * @param logger  slf4j logger
     * @return the chosen one Doris BE node
     * @throws IllegalArgumentException BE nodes is illegal
     *                                  This method is deprecated. Because it
     *                                  needs ADMIN_PRIV to get backends, which
     *                                  is not suitable for common users.
     *                                  Use getBackendsV2 instead
     */
    @Deprecated
    @VisibleForTesting
    static List<BackendRow> getBackends(final DorisOptions options,
                                        final DorisReadOptions readOptions, final Logger logger)
            throws DorisException, IOException {
        final String feNodes = options.getFenodes();
        final String feNode = randomEndpoint(feNodes, logger);
        final String beUrl = "http://" + feNode + BACKENDS;
        final HttpGet httpGet = new HttpGet(beUrl);
        final String response = send(options, readOptions, httpGet, logger);
        logger.info("Backend Info:{}", response);
        final List<BackendRow> backends = parseBackend(response, logger);
        return backends;
    }

    @Deprecated
    static List<BackendRow> parseBackend(final String response,
                                         final Logger logger) throws DorisException, IOException {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        final Backend backend = new Backend();
        try {
            final Map<Object, Object> tmp = mapper.readValue(response,
                    Map.class);
            final List<Map<String, Object>> rows = (List<Map<String, Object>>) tmp
                    .get("rows");
            backend.setRows(rows.stream().map(map -> {
                BackendRow row = new BackendRow();
                row.setIP((String) map.get("IP"));
                row.setHttpPort((String) map.get("HttpPort"));
                row.setAlive(Boolean.valueOf((String) map.get("Alive")));
                return row;
            }).collect(Collectors.toList()));
            logger.info("backends: {}", JsonUtils.toJson(backend));
        } catch (final JsonParseException e) {
            final String errMsg = "Doris BE's response is not a json. res: "
                    + response;
            logger.error(errMsg, e);
            throw new DorisException(errMsg, e);
        } catch (final JsonMappingException e) {
            final String errMsg = "Doris BE's response cannot map to schema. res: "
                    + response;
            logger.error(errMsg, e);
            throw new DorisException(errMsg, e);
        } catch (final IOException e) {
            final String errMsg = "Parse Doris BE's response to json failed. res: "
                    + response;
            logger.error(errMsg, e);
            throw new DorisException(errMsg, e);
        }

        if (backend == null) {
            logger.error(SHOULD_NOT_HAPPEN_MESSAGE);
            throw new ShouldNeverHappenException();
        }
        final List<BackendRow> backendRows = backend.getRows().stream()
                .filter(v -> v.getAlive()).collect(Collectors.toList());
        logger.debug("Parsing schema result is '{}'.", backendRows);
        return backendRows;
    }

    /**
     * get Doris BE nodes to request.
     *
     * @param options configuration of request
     * @param logger  slf4j logger
     * @return all Doris BE node
     * @throws IllegalArgumentException BE nodes is illegal
     */
    @VisibleForTesting
    static List<BackendV2.BackendRowV2> getBackendsV2(
            final DorisOptions options, final DorisReadOptions readOptions,
            final Logger logger) throws DorisException, IOException {
        final String feNodes = options.getFenodes();
        final List<String> feNodeList = allEndpoints(feNodes, logger);
        for (final String feNode : feNodeList) {
            try {
                final String beUrl = "http://" + feNode + BACKENDS_V2;
                final HttpGet httpGet = new HttpGet(beUrl);
                final String response = send(options, readOptions, httpGet,
                        logger);
                logger.info("Backend Info:{}", response);
                final List<BackendV2.BackendRowV2> backends = parseBackendV2(
                        response, logger);
                return backends;
            } catch (final ConnectedFailedException e) {
                logger.info(
                        "Doris FE node {} is unavailable: {}, Request the next Doris FE node",
                        feNode, e.getMessage());
            }
        }
        final String errMsg = "No Doris FE is available, please check configuration";
        logger.error(errMsg);
        throw new DorisException(errMsg);
    }

    static List<BackendV2.BackendRowV2> parseBackendV2(final String response,
                                                       final Logger logger) throws DorisException, IOException {
        final ObjectMapper mapper = new ObjectMapper();
        final BackendV2 backend;
        try {
            backend = mapper.readValue(response, BackendV2.class);
        } catch (final JsonParseException e) {
            final String errMsg = "Doris BE's response is not a json. res: "
                    + response;
            logger.error(errMsg, e);
            throw new DorisException(errMsg, e);
        } catch (final JsonMappingException e) {
            final String errMsg = "Doris BE's response cannot map to schema. res: "
                    + response;
            logger.error(errMsg, e);
            throw new DorisException(errMsg, e);
        } catch (final IOException e) {
            final String errMsg = "Parse Doris BE's response to json failed. res: "
                    + response;
            logger.error(errMsg, e);
            throw new DorisException(errMsg, e);
        }

        if (backend == null) {
            logger.error(SHOULD_NOT_HAPPEN_MESSAGE);
            throw new ShouldNeverHappenException();
        }
        final List<BackendV2.BackendRowV2> backendRows = backend.getBackends();
        logger.debug("Parsing schema result is '{}'.", backendRows);
        return backendRows;
    }

    /**
     * get a valid URI to connect Doris FE.
     *
     * @param options configuration of request
     * @param logger  {@link Logger}
     * @return uri string
     * @throws IllegalArgumentException throw when configuration is illegal
     */
    @VisibleForTesting
    static String getUriStr(final DorisOptions options, final Logger logger)
            throws IllegalArgumentException {
        final String[] identifier = parseIdentifier(
                options.getTableIdentifier(), logger);
        return "http://" + randomEndpoint(options.getFenodes(), logger)
                + API_PREFIX + "/" + identifier[0] + "/" + identifier[1] + "/";
    }

    /**
     * discover Doris table schema from Doris FE.
     *
     * @param options configuration of request
     * @param logger  slf4j logger
     * @return Doris table schema
     * @throws DorisException throw when discover failed
     */
    public static Schema getSchema(final DorisOptions options,
                                   final DorisReadOptions readOptions, final Logger logger)
            throws DorisException {
        logger.trace("Finding schema.");
        final HttpGet httpGet = new HttpGet(
                getUriStr(options, logger) + SCHEMA);
        final String response = send(options, readOptions, httpGet, logger);
        logger.debug("Find schema response is '{}'.", response);
        return parseSchema(response, logger);
    }

    /**
     * translate Doris FE response to inner {@link Schema} struct.
     *
     * @param response Doris FE response
     * @param logger   {@link Logger}
     * @return inner {@link Schema} struct
     * @throws DorisException throw when translate failed
     */
    @VisibleForTesting
    public static Schema parseSchema(final String response, final Logger logger)
            throws DorisException {
        logger.trace("Parse response '{}' to schema.", response);
        final ObjectMapper mapper = new ObjectMapper();
        final Schema schema;
        try {
            schema = mapper.readValue(response, Schema.class);
        } catch (final JsonParseException e) {
            final String errMsg = "Doris FE's response is not a json. res: "
                    + response;
            logger.error(errMsg, e);
            throw new DorisException(errMsg, e);
        } catch (final JsonMappingException e) {
            final String errMsg = "Doris FE's response cannot map to schema. res: "
                    + response;
            logger.error(errMsg, e);
            throw new DorisException(errMsg, e);
        } catch (final IOException e) {
            final String errMsg = "Parse Doris FE's response to json failed. res: "
                    + response;
            logger.error(errMsg, e);
            throw new DorisException(errMsg, e);
        }

        if (schema == null) {
            logger.error(SHOULD_NOT_HAPPEN_MESSAGE);
            throw new ShouldNeverHappenException();
        }

        if (schema.getStatus() != REST_RESPONSE_STATUS_OK) {
            final String errMsg = "Doris FE's response is not OK, status is "
                    + schema.getStatus();
            logger.error(errMsg);
            throw new DorisException(errMsg);
        }
        logger.debug("Parsing schema result is '{}'.", schema);
        return schema;
    }

    /**
     * find Doris partitions from Doris FE.
     *
     * @param options configuration of request
     * @param logger  {@link Logger}
     * @return an list of Doris partitions
     * @throws DorisException throw when find partition failed
     */
    public static List<PartitionDefinition> findPartitions(
            final DorisOptions options, final DorisReadOptions readOptions,
            final Logger logger) throws DorisException {
        final String[] tableIdentifiers = parseIdentifier(
                options.getTableIdentifier(), logger);
        final String readFields = StringUtils
                .isBlank(readOptions.getReadFields()) ? "*"
                : readOptions.getReadFields();
        String sql = "select " + readFields + " from `" + tableIdentifiers[0]
                + "`.`" + tableIdentifiers[1] + "`";
        if (!StringUtils.isEmpty(readOptions.getFilterQuery())) {
            sql += " where " + readOptions.getFilterQuery();
        }
        logger.debug("Query SQL Sending to Doris FE is: '{}'.", sql);

        final HttpPost httpPost = new HttpPost(
                getUriStr(options, logger) + QUERY_PLAN);
        final String entity = "{\"sql\": \"" + sql + "\"}";
        logger.debug("Post body Sending to Doris FE is: '{}'.", entity);
        final StringEntity stringEntity = new StringEntity(entity,
                StandardCharsets.UTF_8);
        stringEntity.setContentEncoding("UTF-8");
        stringEntity.setContentType("application/json");
        httpPost.setEntity(stringEntity);

        final String resStr = send(options, readOptions, httpPost, logger);
        logger.debug("Find partition response is '{}'.", resStr);
        final QueryPlan queryPlan = getQueryPlan(resStr, logger);
        final Map<String, List<Long>> be2Tablets = selectBeForTablet(queryPlan,
                logger);
        return tabletsMapToPartition(options, readOptions, be2Tablets,
                queryPlan.getOpaqued_query_plan(), tableIdentifiers[0],
                tableIdentifiers[1], logger);
    }

    /**
     * translate Doris FE response string to inner {@link QueryPlan} struct.
     *
     * @param response Doris FE response string
     * @param logger   {@link Logger}
     * @return inner {@link QueryPlan} struct
     * @throws DorisException throw when translate failed.
     */
    @VisibleForTesting
    static QueryPlan getQueryPlan(final String response, final Logger logger)
            throws DorisException {
        final ObjectMapper mapper = new ObjectMapper();
        final QueryPlan queryPlan;
        try {
            queryPlan = mapper.readValue(response, QueryPlan.class);
        } catch (final JsonParseException e) {
            final String errMsg = "Doris FE's response is not a json. res: "
                    + response;
            logger.error(errMsg, e);
            throw new DorisException(errMsg, e);
        } catch (final JsonMappingException e) {
            final String errMsg = "Doris FE's response cannot map to schema. res: "
                    + response;
            logger.error(errMsg, e);
            throw new DorisException(errMsg, e);
        } catch (final IOException e) {
            final String errMsg = "Parse Doris FE's response to json failed. res: "
                    + response;
            logger.error(errMsg, e);
            throw new DorisException(errMsg, e);
        }

        if (queryPlan == null) {
            logger.error(SHOULD_NOT_HAPPEN_MESSAGE);
            throw new ShouldNeverHappenException();
        }

        if (queryPlan.getStatus() != REST_RESPONSE_STATUS_OK) {
            final String errMsg = "Doris FE's response is not OK, status is "
                    + queryPlan.getStatus();
            logger.error(errMsg);
            throw new DorisException(errMsg);
        }
        logger.debug("Parsing partition result is '{}'.", queryPlan);
        return queryPlan;
    }

    /**
     * select which Doris BE to get tablet data.
     *
     * @param queryPlan {@link QueryPlan} translated from Doris FE response
     * @param logger    {@link Logger}
     * @return BE to tablets {@link Map}
     * @throws DorisException throw when select failed.
     */
    @VisibleForTesting
    static Map<String, List<Long>> selectBeForTablet(final QueryPlan queryPlan,
                                                     final Logger logger) throws DorisException {
        final Map<String, List<Long>> be2Tablets = new HashMap<>();
        for (final Map.Entry<String, Tablet> part : queryPlan.getPartitions()
                .entrySet()) {
            logger.debug("Parse tablet info: '{}'.", part);
            final long tabletId;
            try {
                tabletId = Long.parseLong(part.getKey());
            } catch (final NumberFormatException e) {
                final String errMsg = "Parse tablet id '" + part.getKey()
                        + "' to long failed.";
                logger.error(errMsg, e);
                throw new DorisException(errMsg, e);
            }
            String target = null;
            int tabletCount = Integer.MAX_VALUE;
            for (final String candidate : part.getValue().getRoutings()) {
                logger.trace("Evaluate Doris BE '{}' to tablet '{}'.",
                        candidate, tabletId);
                if (!be2Tablets.containsKey(candidate)) {
                    logger.debug("Choice a new Doris BE '{}' for tablet '{}'.",
                            candidate, tabletId);
                    final List<Long> tablets = new ArrayList<>();
                    be2Tablets.put(candidate, tablets);
                    target = candidate;
                    break;
                } else {
                    if (be2Tablets.get(candidate).size() < tabletCount) {
                        target = candidate;
                        tabletCount = be2Tablets.get(candidate).size();
                        logger.debug(
                                "Current candidate Doris BE to tablet '{}' is '{}' with tablet count {}.",
                                tabletId, target, tabletCount);
                    }
                }
            }
            if (target == null) {
                final String errMsg = "Cannot choice Doris BE for tablet "
                        + tabletId;
                logger.error(errMsg);
                throw new DorisException(errMsg);
            }

            logger.debug("Choice Doris BE '{}' for tablet '{}'.", target,
                    tabletId);
            be2Tablets.get(target).add(tabletId);
        }
        return be2Tablets;
    }

    /**
     * tablet count limit for one Doris RDD partition
     *
     * @param readOptions configuration of request
     * @param logger      {@link Logger}
     * @return tablet count limit
     */
    @VisibleForTesting
    static int tabletCountLimitForOnePartition(
            final DorisReadOptions readOptions, final Logger logger) {
        int tabletsSize = DORIS_TABLET_SIZE_DEFAULT;
        if (readOptions.getRequestTabletSize() != null) {
            tabletsSize = readOptions.getRequestTabletSize();
        }
        if (tabletsSize < DORIS_TABLET_SIZE_MIN) {
            logger.warn("{} is less than {}, set to default value {}.",
                    DORIS_TABLET_SIZE, DORIS_TABLET_SIZE_MIN,
                    DORIS_TABLET_SIZE_MIN);
            tabletsSize = DORIS_TABLET_SIZE_MIN;
        }
        logger.debug("Tablet size is set to {}.", tabletsSize);
        return tabletsSize;
    }

    /**
     * translate BE tablets map to Doris RDD partition.
     *
     * @param options          configuration of request
     * @param be2Tablets       BE to tablets {@link Map}
     * @param opaquedQueryPlan Doris BE execute plan getting from Doris FE
     * @param database         database name of Doris table
     * @param table            table name of Doris table
     * @param logger           {@link Logger}
     * @return Doris RDD partition {@link List}
     * @throws IllegalArgumentException throw when translate failed
     */
    @VisibleForTesting
    static List<PartitionDefinition> tabletsMapToPartition(
            final DorisOptions options, final DorisReadOptions readOptions,
            final Map<String, List<Long>> be2Tablets,
            final String opaquedQueryPlan, final String database,
            final String table, final Logger logger)
            throws IllegalArgumentException {
        final int tabletsSize = tabletCountLimitForOnePartition(readOptions,
                logger);
        final List<PartitionDefinition> partitions = new ArrayList<>();
        for (final Map.Entry<String, List<Long>> beInfo : be2Tablets
                .entrySet()) {
            logger.debug("Generate partition with beInfo: '{}'.", beInfo);
            final HashSet<Long> tabletSet = new HashSet<>(beInfo.getValue());
            beInfo.getValue().clear();
            beInfo.getValue().addAll(tabletSet);
            int first = 0;
            while (first < beInfo.getValue().size()) {
                final Set<Long> partitionTablets = new HashSet<>(
                        beInfo.getValue().subList(first,
                                Math.min(beInfo.getValue().size(),
                                        first + tabletsSize)));
                first = first + tabletsSize;
                final PartitionDefinition partitionDefinition = new PartitionDefinition(
                        database, table, options, beInfo.getKey(),
                        partitionTablets, opaquedQueryPlan);
                logger.debug("Generate one PartitionDefinition '{}'.",
                        partitionDefinition);
                partitions.add(partitionDefinition);
            }
        }
        return partitions;
    }

}
