package com.sivannsan.millidb;

import com.sivannsan.foundation.annotation.Nonnull;
import com.sivannsan.foundation.Validate;
import com.sivannsan.millidata.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;

public final class MilliDBClients {
    private MilliDBClients() {
    }

    /**
     * Connect to the server
     *
     * @param host          the host name or address of the server
     * @param port          the port of the server
     * @param userName      the name of the user that is used to access the files
     * @param userPassword  the password of the user that is used to access the files
     * @return  the client corresponding to the connected server, null if the client is not fully formed
     */
    public static MilliDBClient connect(@Nonnull String host, int port, @Nonnull String userName, @Nonnull String userPassword) {
        try {
            MilliDBLogger.info("Connecting to a MilliDBServer...");
            MilliDBLogger.info("- host: " + host);
            MilliDBLogger.info("- port: " + port);
            long time = System.currentTimeMillis();
            Socket socket = new Socket(host, port);
            //TODO: Study about these reader and writer streams
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            socket.setSoTimeout(5000);
            IMilliDBClient client = new IMilliDBClient(socket, reader, writer, 5);
            MilliDBResult result = client.execute(new MilliDBQuery("", MilliDBQuery.Function.HAS_USER, new MilliMap().append("user_name", new MilliValue(userName)).append("user_password", new MilliValue(userPassword))));
            if (!result.isSucceed()) {
                MilliDBLogger.warning("Failed to verify user!");
                return null;
            }
            if (!result.getMetadata().asMilliValue(new MilliValue(false)).asBoolean()) {
                MilliDBLogger.warning("The input user does not exists or is incorrect password!");
                return null;
            }
            MilliDBLogger.info("The MilliDBServer has connected in " + (System.currentTimeMillis() - time) + "ms!");
            return client;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static final class IMilliDBClient implements MilliDBClient {
        @Nonnull
        private final Socket socket;
        @Nonnull
        private final BufferedReader reader;
        @Nonnull
        private final PrintWriter writer;
        private final int maxFailures;
        private static final Map<MilliDBQuery, Integer> executeCounters = new HashMap<>();

        private IMilliDBClient(@Nonnull Socket socket, @Nonnull BufferedReader reader, @Nonnull PrintWriter writer, int maxFailures) throws IOException {
            this.socket = Validate.nonnull(socket);
            this.reader = Validate.nonnull(reader);
            this.writer = Validate.nonnull(writer);
            this.maxFailures = maxFailures;
        }

        @Override
        @Nonnull
        public List<MilliDBFile> getFiles() throws MilliDBResultFailedException {
            return getFiles(null);
        }

        @Override
        public List<MilliDBFile> getFiles(MilliDBFilter filter) throws MilliDBResultFailedException {
            MilliDBResult result = execute(new MilliDBQuery("", MilliDBQuery.Function.GET_FILES, filter == null ? MilliNull.INSTANCE : filter.toMilliMap()));
            if (!result.isSucceed()) throw new MilliDBResultFailedException();
            List<MilliDBFile> files = new ArrayList<>();
            for (MilliData data : result.getMetadata().asMilliList(new MilliList())) {
                String name = data.asMilliValue().asString();
                MilliDBFile file;
                if (name.endsWith(".mll")) file = new IMilliDBDocument(this, null, name);
                else file = new IMilliDBCollection(this, null, name);
                files.add(file);
            }
            return files;
        }

        @Override
        @Nonnull
        public MilliDBDocument getDocument(@Nonnull String name) throws MilliDBResultFailedException, MilliDBPermissionException, IllegalArgumentException {
            Validate.nonnull(name);
            if (!name.endsWith(".mll")) throw new IllegalArgumentException("Invalid MilliDBDocument name!");
            MilliDBResult result = execute(new MilliDBQuery("", MilliDBQuery.Function.GET_DOCUMENT, new MilliValue(name)));
            if (!result.isSucceed()) throw new MilliDBResultFailedException();
            if (!result.getMetadata().asMilliValue(new MilliValue(false)).asBoolean()) throw new MilliDBPermissionException();
            return new IMilliDBDocument(this, null, name);
        }

        @Override
        @Nonnull
        public MilliDBCollection getCollection(String name) throws MilliDBResultFailedException, MilliDBPermissionException, IllegalArgumentException {
            Validate.nonnull(name);
            if (name.endsWith(".mll")) throw new IllegalArgumentException("Invalid MilliDBCollection name!");
            MilliDBResult result = execute(new MilliDBQuery("", MilliDBQuery.Function.GET_COLLECTION, new MilliValue(name)));
            if (!result.isSucceed()) throw new MilliDBResultFailedException();
            if (!result.getMetadata().asMilliValue(new MilliValue(false)).asBoolean()) throw new MilliDBPermissionException();
            return new IMilliDBCollection(this, null, name);
        }

        @Nonnull
        public MilliDBResult execute(@Nonnull MilliDBQuery query) {
            executeCounters.put(query, executeCounters.getOrDefault(query, 0) + 1);
            int counter = executeCounters.get(query);
            if (counter > maxFailures) {
                executeCounters.remove(query);
                return MilliDBResult.failedResult(query.getID());
            }
            if (counter > 1) MilliDBLogger.warning("Query '" + query.getID() + "' has failed to execute " + (counter - 1) + " time" + (counter > 2 ? "s" : ""));
            writer.println(query.asMilliMap().toString());
            try {
                MilliDBResult result = MilliDBResult.Parser.parse(reader.readLine());
                if (result.getID() == query.getID()) return result;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return execute(query);
        }
    }

    private static abstract class IMilliDBFile implements MilliDBFile {
        private final IMilliDBClient client;
        private final MilliDBCollection parent;
        @Nonnull
        private final String name;

        protected IMilliDBFile(@Nonnull IMilliDBClient client, MilliDBCollection parent, @Nonnull String name) {
            this.client = Validate.nonnull(client);
            this.parent = parent;
            this.name = Validate.nonnull(name);
        }

        @Nonnull
        protected IMilliDBClient getClient() {
            return client;
        }

        @Override
        public final MilliDBCollection getParent() {
            return parent;
        }

        @Override
        @Nonnull
        public final String getPath() {
            return parent == null ? name : parent.getPath() + "/" + name;
        }

        @Override
        @Nonnull
        public final String getName() {
            return name;
        }

        @Override
        public final boolean isMilliDBDocument() {
            return this instanceof MilliDBDocument;
        }

        @Override
        public final boolean isMilliDBCollection() {
            return this instanceof MilliDBCollection;
        }

        @Override
        @Nonnull
        public final MilliDBDocument asMilliDBDocument() throws ClassCastException {
            if (isMilliDBDocument()) return (MilliDBDocument) this;
            throw new ClassCastException("Not a MilliDBDocument");
        }

        @Override
        @Nonnull
        public final MilliDBCollection asMilliDBCollection() throws ClassCastException {
            if (isMilliDBCollection()) return (MilliDBCollection) this;
            throw new ClassCastException("Not a MilliDBCollection");
        }

        @Override
        public final void delete() throws MilliDBResultFailedException {
            MilliDBResult result = getClient().execute(new MilliDBQuery(getPath(), MilliDBQuery.Function.DELETE, new MilliValue(name)));
            if (!result.isSucceed()) throw new MilliDBResultFailedException();
        }
    }

    private static final class IMilliDBDocument extends IMilliDBFile implements MilliDBDocument {
        public IMilliDBDocument(@Nonnull IMilliDBClient client, MilliDBCollection parent, @Nonnull String name) {
            super(client, parent, name);
        }

        @Override
        @Nonnull
        public MilliData get(@Nonnull String path) {
            MilliDBResult result = getClient().execute(new MilliDBQuery(getPath(), MilliDBQuery.Function.GET, new MilliValue(path)));
            if (!result.isSucceed()) throw new MilliDBResultFailedException();
            return result.getMetadata();
        }

        @Override
        public void set(@Nonnull String path, @Nonnull MilliData value) throws MilliDBResultFailedException {
            MilliDBResult result = getClient().execute(new MilliDBQuery(getPath(), MilliDBQuery.Function.SET, new MilliMap().append("p", new MilliValue(path)).append("v", value)));
            if (!result.isSucceed()) throw new MilliDBResultFailedException();
        }

        @Override
        @Nonnull
        public MilliData getContent() throws MilliDBResultFailedException {
            return get("");
        }

        @Override
        public void setContent(@Nonnull MilliData value) throws MilliDBResultFailedException {
            set("", value);
        }
    }

    private static final class IMilliDBCollection extends IMilliDBFile implements MilliDBCollection {
        public IMilliDBCollection(@Nonnull IMilliDBClient client, MilliDBCollection parent, @Nonnull String name) {
            super(client, parent, name);
        }

        @Override
        @Nonnull
        public List<MilliDBFile> getFiles() throws MilliDBResultFailedException {
            return getFiles(null);
        }

        @Override
        @Nonnull
        public List<MilliDBFile> getFiles(MilliDBFilter filter) throws MilliDBResultFailedException {
            MilliDBResult result = getClient().execute(new MilliDBQuery(getPath(), MilliDBQuery.Function.GET_FILES, filter == null ? MilliNull.INSTANCE : filter.toMilliMap()));
            if (!result.isSucceed()) throw new MilliDBResultFailedException();
            List<MilliDBFile> files = new ArrayList<>();
            for (MilliData data : result.getMetadata().asMilliList(new MilliList())) {
                String name = data.asMilliValue().asString();
                MilliDBFile file;
                if (name.endsWith(".mll")) file = new IMilliDBDocument(getClient(), this, name);
                else file = new IMilliDBCollection(getClient(), this, name);
                files.add(file);
            }
            return files;
        }

        @Override
        @Nonnull
        public MilliDBDocument getDocument(String name) throws MilliDBResultFailedException, MilliDBPermissionException, IllegalArgumentException {
            Validate.nonnull(name);
            if (!name.endsWith(".mll")) throw new IllegalArgumentException("Invalid MilliDBDocument name!");
            MilliDBResult result = getClient().execute(new MilliDBQuery("", MilliDBQuery.Function.GET_DOCUMENT, new MilliValue(name)));
            if (!result.isSucceed()) throw new MilliDBResultFailedException();
            if (!result.getMetadata().asMilliValue(new MilliValue(false)).asBoolean()) throw new MilliDBPermissionException();
            return new IMilliDBDocument(getClient(), this, name);
        }

        @Override
        @Nonnull
        public MilliDBCollection getCollection(String name) throws MilliDBResultFailedException, MilliDBPermissionException, IllegalArgumentException {
            Validate.nonnull(name);
            if (name.endsWith(".mll")) throw new IllegalArgumentException("Invalid MilliDBCollection name!");
            MilliDBResult result = getClient().execute(new MilliDBQuery("", MilliDBQuery.Function.GET_COLLECTION, new MilliValue(name)));
            if (!result.isSucceed()) throw new MilliDBResultFailedException();
            if (!result.getMetadata().asMilliValue(new MilliValue(false)).asBoolean()) throw new MilliDBPermissionException();
            return new IMilliDBCollection(getClient(), this, name);
        }

        @Override
        public Iterator<MilliDBFile> iterator() {
            return getFiles().iterator();
        }
    }
}
