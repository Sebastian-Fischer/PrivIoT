package eu.spitfire.ssp.backends.internal.vs;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import eu.spitfire.ssp.server.internal.messages.responses.DataOriginAccessError;
import eu.spitfire.ssp.server.internal.messages.responses.DataOriginInquiryResult;
import eu.spitfire.ssp.backends.generic.Accessor;
import eu.spitfire.ssp.backends.generic.BackendComponentFactory;
import eu.spitfire.ssp.server.internal.messages.requests.QueryTask;
import eu.spitfire.ssp.server.internal.messages.responses.AccessResult;
import eu.spitfire.ssp.server.internal.messages.responses.ExpiringNamedGraph;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.Channels;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

/**
 * Created by olli on 06.05.14.
 */
public class VirtualSensorAccessor extends Accessor<URI, VirtualSensor> {

    private Logger log = LoggerFactory.getLogger(this.getClass().getName());

    /**
     * Creates a new instance of {@link eu.spitfire.ssp.backends.generic.Accessor}
     *
     * @param componentFactory
     */
    protected VirtualSensorAccessor(BackendComponentFactory<URI, VirtualSensor> componentFactory) {
        super(componentFactory);
    }


    @Override
    public ListenableFuture<DataOriginInquiryResult> getStatus(final VirtualSensor virtualSensor){
        log.info("Try to get status for data origin with identifier {}", virtualSensor.getIdentifier());

        final SettableFuture<DataOriginInquiryResult> graphStatusFuture = SettableFuture.create();

//        String query = String.format("SELECT ?s ?p ?o FROM <%s> WHERE {?s ?p ?o}", virtualSensor.getGraphName());
        String query = String.format(
                "PREFIX ssn: <http://purl.oclc.org/NET/ssnx/ssn#>\n" +
                "SELECT ?value WHERE {<%s-SensorOutput> ssn:hasValue ?value }", virtualSensor.getGraphName()
        );

        Query sparqlQuery = QueryFactory.create(query);

        SettableFuture<ResultSet> resultSetFuture = SettableFuture.create();

        ChannelFuture channelFuture = Channels.write(
                this.getComponentFactory().getLocalChannel(), new QueryTask(sparqlQuery, resultSetFuture)
        );

        channelFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if(!future.isSuccess()){
                    log.error("Exception during retrieval of virtual sensor status (Graph: {})!",
                            virtualSensor.getGraphName(), future.getCause());
                }
            }
        });

        Futures.addCallback(resultSetFuture, new FutureCallback<ResultSet>() {
            @Override
            public void onSuccess(ResultSet resultSet) {
                if (resultSet == null || !resultSet.hasNext()) {
                    DataOriginAccessError dataOriginAccessError = new DataOriginAccessError(
                            AccessResult.Code.NOT_FOUND,
                            String.format("Graph %s not found!", virtualSensor.getGraphName())
                    );

                    graphStatusFuture.set(dataOriginAccessError);
                    return;
                }

                Model vsModel = ModelFactory.createDefaultModel();

                while (resultSet.hasNext()) {
                    Binding binding = resultSet.nextBinding();

                    Node subject = binding.get(Var.alloc("s"));
                    Node predicate = binding.get(Var.alloc("p"));
                    Node object = binding.get(Var.alloc("o"));

                    Resource s = subject.isBlank() ? ResourceFactory.createResource(subject.getBlankNodeLabel()) :
                            ResourceFactory.createResource(subject.getURI());

                    Property p = ResourceFactory.createProperty(predicate.getURI());

                    RDFNode o = object.isBlank() ? ResourceFactory.createResource(object.getBlankNodeLabel()) :
                            object.isLiteral() ? ResourceFactory.createPlainLiteral(object.getLiteralLexicalForm()) :
                                    ResourceFactory.createResource(object.getURI());

                    vsModel.add(vsModel.createStatement(s, p, o));
                }

                graphStatusFuture.set(new ExpiringNamedGraph(virtualSensor.getGraphName(), vsModel));
            }

            @Override
            public void onFailure(Throwable t) {
                log.error("Failed to retrieve virtual sensor status (Graph: {})", virtualSensor.getGraphName(), t);
                graphStatusFuture.setException(t);
            }
        });

        return graphStatusFuture;
    }



}
