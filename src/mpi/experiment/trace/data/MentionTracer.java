package mpi.experiment.trace.data;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import mpi.aida.data.Mention;

public class MentionTracer {

	private Map<String, EntityTracer> entities = new HashMap<String, EntityTracer>();

	private Mention mention;

	public MentionTracer(Mention mention) {
		this.mention = mention;
	}

	public String getName() {
		return mention.getMention();
	}

	public EntityTracer getEntityTracer(String entity) {
		return entities.get(entity);
	}

	public int getOffset() {
		return mention.getCharOffset();
	}

	public void addEntityTracer(String entity, EntityTracer entityTracer) {
		entities.put(entity, entityTracer);
	}

	public Collection<EntityTracer> getEntityTracers() {
		return entities.values();
	}

	public int getLength() {
		return mention.getCharLength();
	}
	
	public String getMentionStr() {
		return mention.getMention() + ":" + mention.getStartToken();
	}
}
