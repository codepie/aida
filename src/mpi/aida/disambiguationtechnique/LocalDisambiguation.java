package mpi.aida.disambiguationtechnique;

import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import mpi.aida.AidaManager;
import mpi.aida.data.Entities;
import mpi.aida.data.Entity;
import mpi.aida.data.Mention;
import mpi.aida.data.PreparedInput;
import mpi.aida.data.ResultEntity;
import mpi.aida.data.ResultMention;
import mpi.aida.graph.similarity.EnsembleMentionEntitySimilarity;
import mpi.aida.graph.similarity.util.SimilaritySettings;
import mpi.experiment.trace.Tracer;
import mpi.experiment.trace.data.EntityTracer;
import mpi.experiment.trace.data.MentionTracer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalDisambiguation implements Runnable {
  private static final Logger logger = 
      LoggerFactory.getLogger(LocalDisambiguation.class);
  
	protected SimilaritySettings ss;

	protected PreparedInput input;

	protected String docId;

	protected Map<String, Map<ResultMention, List<ResultEntity>>> solutions;

	protected boolean includeNullAsEntityCandidate;
	
	protected boolean includeContextMentions;

	protected Tracer tracer = null;

	private NumberFormat nf;
	
	public LocalDisambiguation(PreparedInput input, SimilaritySettings settings,
			boolean includeNullAsEntityCandidate,
			boolean includeContextMentions, String docId,
			Map<String, Map<ResultMention, List<ResultEntity>>> solutions, Tracer tracer)
			throws SQLException {
	  nf = NumberFormat.getNumberInstance(Locale.ENGLISH);
	  nf.setMaximumFractionDigits(2);
	  logger.debug("Preparing '" + docId + "' (" + input.getMentions().getMentions().size() + " mentions)");

		this.ss = settings;
		this.docId = docId;
		this.solutions = solutions;
		this.input = input;
		this.includeNullAsEntityCandidate = includeNullAsEntityCandidate;
		this.includeContextMentions = includeContextMentions;
		this.tracer = tracer;
		
		logger.debug("Finished preparing '" + docId + "'");
	}

	@Override
	public void run() {	  
	  long beginTime = System.currentTimeMillis();
	  try {
      AidaManager.fillInCandidateEntities(docId, input.getMentions(),
          includeNullAsEntityCandidate, includeContextMentions);
    } catch (SQLException e) {
      logger.error("SQLException when getting candidates: " + 
                   e.getLocalizedMessage());
    }
   
		EnsembleMentionEntitySimilarity mes = prepapreMES();

		try {
      disambiguate(mes);
    } catch (Exception e) {
      logger.error("Error: " + e.getLocalizedMessage());
    }
		double runTime = (System.currentTimeMillis() - beginTime) / (double) 1000;
		logger.info("Document '" + docId + "' done in " + nf.format(runTime) + "s");
	}
	
	private EnsembleMentionEntitySimilarity prepapreMES() {
		Entities entities = new Entities();
		for (Mention mention : input.getMentions().getMentions()) {
			MentionTracer mt = new MentionTracer(mention);
			tracer.addMentionForDocId(docId, mention, mt);
			for (Entity entity : mention.getCandidateEntities()) {
				EntityTracer et = new EntityTracer(entity.getName());
				tracer.addEntityForMention(mention, entity.getName(), et);
			}
			entities.addAll(mention.getCandidateEntities());
		}
		
		logger.info("Disambiguating '" + docId + 
		         "' (" + input.getMentions().getMentions().size() + 
		         " mentions, " + entities.size() + " entities)");
		
		if (includeNullAsEntityCandidate) {
			entities.setIncludesNmeEntities(true);
		}

		EnsembleMentionEntitySimilarity mes = null;
		try {
			mes = new EnsembleMentionEntitySimilarity(input.getMentions(), entities, ss,
					docId, tracer);
			return mes;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	protected void disambiguate(EnsembleMentionEntitySimilarity mes) throws Exception {
		for (Mention mention : input.getMentions().getMentions()) {		
			List<ResultEntity> entities = new LinkedList<ResultEntity>();
			
			for (Entity entity : mention.getCandidateEntities()) { 
			  double sim = mes.calcSimilarity(mention, input.getContext(), entity);
				entities.add(new ResultEntity(entity.getName(), sim));
			}
			
			if (!entities.isEmpty()) {
			  Collections.sort(entities);
			} else {
			  entities.add(ResultEntity.getNoMatchingEntity());
			}

			Map<ResultMention, List<ResultEntity>> docSolutions = solutions.get(docId);
			if (docSolutions == null) {
				docSolutions = new HashMap<ResultMention, List<ResultEntity>>();
				solutions.put(docId, docSolutions);
			}
			
			ResultMention rm = new ResultMention(docId, mention.getMention(), mention.getCharOffset(), mention.getCharLength());

			docSolutions.put(rm, entities);
		}
	}
}
