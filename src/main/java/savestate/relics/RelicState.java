package savestate.relics;

import basemod.ReflectionHacks;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.helpers.RelicLibrary;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import savestate.StateFactories;

import java.util.function.Function;

public class RelicState {
    public final String relicId;
    public final int counter;
    private final boolean grayscale;
    private final boolean pulse;

    public RelicState(AbstractRelic relic) {
        this.relicId = relic.relicId;
        this.counter = relic.counter;
        this.grayscale = relic.grayscale;
        this.pulse = ReflectionHacks.getPrivate(relic, AbstractRelic.class, "pulse");
    }

    public RelicState(String jsonString) {
        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.relicId = parsed.get("relic_id").getAsString();
        this.counter = parsed.get("counter").getAsInt();
        this.grayscale = parsed.get("grayscale").getAsBoolean();
        this.pulse = parsed.get("pulse").getAsBoolean();
    }

    public AbstractRelic loadRelic() {
        AbstractRelic result;

        long makeRelicCopyStartTime = System.currentTimeMillis();

        result = RelicLibrary.getRelic(relicId).makeCopy();

        result.counter = counter;
        result.grayscale = grayscale;

        ReflectionHacks.setPrivate(result, AbstractRelic.class, "pulse", pulse);

        return result;
    }

    public String encode() {
        JsonObject relicStateJson = new JsonObject();

        relicStateJson.addProperty("relic_id", relicId);
        relicStateJson.addProperty("counter", counter);
        relicStateJson.addProperty("grayscale", grayscale);
        relicStateJson.addProperty("pulse", pulse);

        return relicStateJson.toString();
    }

    public static RelicState forRelic(AbstractRelic relic) {
        if (StateFactories.relicByIdMap.containsKey(relic.relicId)) {
            return StateFactories.relicByIdMap.get(relic.relicId).factory.apply(relic);
        }

        return new RelicState(relic);
    }

    public static RelicState forJsonString(String jsonString) {
        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        String relicId = parsed.get("relic_id").getAsString();
        if (StateFactories.relicByIdMap.containsKey(relicId)) {
            return StateFactories.relicByIdMap.get(relicId).jsonFactory.apply(jsonString);
        }

        return new RelicState(jsonString);
    }

    public static class RelicFactories {
        public final Function<AbstractRelic, RelicState> factory;
        public final Function<String, RelicState> jsonFactory;

        public RelicFactories(Function<AbstractRelic, RelicState> factory, Function<String, RelicState> jsonFactory) {
            this.factory = factory;
            this.jsonFactory = jsonFactory;
        }

        public RelicFactories(Function<AbstractRelic, RelicState> factory) {
            this.factory = factory;
            this.jsonFactory = json -> new RelicState(json);
        }
    }
}
