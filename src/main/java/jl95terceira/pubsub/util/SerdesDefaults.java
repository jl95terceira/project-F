package jl95terceira.pubsub.util;

import java.util.Base64;
import java.util.List;

import javax.json.JsonValue;

import jl95terceira.lang.variadic.*;
import jl95terceira.pubsub.protocol.Message;
import jl95terceira.pubsub.protocol.Publication;
import jl95terceira.pubsub.serdes.PublicationJsonSerdes;
import jl95terceira.pubsub.serdes.Serializer;
import jl95terceira.serdes.*;

public class SerdesDefaults {

    public static final Function1<JsonValue, String>
                                    stringToJson             = StringToJson  .get();
    public static final Function1<String, JsonValue>
                                    stringFromJson           = StringFromJson.get();
    public static final Function1<String, JsonValue>
                                    jsonToString             = JsonToString.get();
    public static final Function1<JsonValue, String>
                                    jsonFromString           = JsonFromString.get();
    public static final Function1<? extends JsonValue, Message<Publication>>
                                    publicationMessageToJson = Serializer.get(MessageType.PUBLISH.serial, PublicationJsonSerdes::toJson);
    public static final Function1<String, byte[]>
                                    bytesToString            = Base64.getEncoder()::encodeToString;
    public static final Function1<byte[], String>
                                    bytesFromString          = Base64.getDecoder()::decode;
    public static final Function1<JsonValue, Iterable<String>>
                                    listOfStringToJson       = ListOfStringToJson.get();
    public static final Function1<List<String>, JsonValue>
                                    listOfStringFromJson     = ListOfStringFromJson.get();
}
