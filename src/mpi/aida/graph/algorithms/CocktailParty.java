package mpi.aida.graph.algorithms;

import gnu.trove.iterator.TIntDoubleIterator;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.linked.TIntLinkedList;
import gnu.trove.map.hash.TIntDoubleHashMap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import mpi.aida.data.Mention;
import mpi.aida.data.ResultEntity;
import mpi.aida.data.ResultMention;
import mpi.aida.graph.Graph;
import mpi.aida.graph.GraphNode;
import mpi.aida.graph.GraphNodeTypes;
import mpi.aida.graph.extraction.DegreeComparator;
import mpi.experiment.trace.GraphTracer;
import mpi.experiment.trace.NullGraphTracer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CocktailParty extends DisambiguationAlgorithm {
	private static final Logger logger = LoggerFactory
			.getLogger(CocktailParty.class);

	protected ShortestPath shortestPath;
	protected Graph graph;

	private boolean useExhaustiveSearch;
	private Map<ResultMention, List<ResultEntity>> solution;

	protected Map<Integer, Double> entityWeightedDegrees = new HashMap<Integer, Double>();

	private Map<Integer, Double> notRemovableEntities = new HashMap<Integer, Double>();
	private PriorityQueue<String> entitySortedDegrees = new PriorityQueue<String>(
			2000, new DegreeComparator());
	private PriorityQueue<String> notRemovableSorted = new PriorityQueue<String>(
			2000, new DegreeComparator());
	protected Map<Integer, Integer> mentionDegrees = new HashMap<Integer, Integer>();
	private Map<Integer, Integer> bestMentionDegrees = new HashMap<Integer, Integer>();
	private Map<Integer, Double> bestWeightedDegrees = new HashMap<Integer, Double>();
	private boolean[] bestRemoved;

	private double entityAlpha = 1.0;

	private double threshold;

	private boolean useNormalizedObjective = true;

	private boolean isTracing = false;

	public CocktailParty(Graph graph, boolean useExhaustiveSearch,
			boolean useNormalizedObjective) throws IOException,
			ClassNotFoundException, InterruptedException {

		if (!(GraphTracer.gTracer instanceof NullGraphTracer))
			isTracing = true;

		this.shortestPath = new ShortestPath();
		this.useExhaustiveSearch = useExhaustiveSearch;
		this.useNormalizedObjective = useNormalizedObjective;
		this.graph = graph;
		bestRemoved = new boolean[graph.getNodesCount()];
	}

	public void setExhaustiveSearch(boolean es) {
		this.useExhaustiveSearch = es;
	}

	public Map<ResultMention, List<ResultEntity>> disambiguate()
			throws Exception {
		solution = new HashMap<ResultMention, List<ResultEntity>>();

		int diameter = getDiameter();
		long start = System.currentTimeMillis();
		// Rescale the distance threshold by the global average distance
		// (derived by the average edge weight)
		double globalAverageWeight = (graph.getAverageMEweight() + graph
				.getAverageEEweight()) / 2.0;

		threshold = diameter * (1 - globalAverageWeight) * 0.5;
		
		debugAndTraceInitialGraphProperties(diameter, globalAverageWeight);
		
		Arrays.fill(bestRemoved, false);

		double initialObjective = firstScanAndCalculateInitialObjective();

		if (isTracing) {
			traceIntitialGraphStructure();
		}

		Set<Integer> bestNotRemovable = notRemovableEntities.keySet();
		Set<Integer> bestRemovable = entityWeightedDegrees.keySet();
		double bestValue = initialObjective;
		boolean noMinRemoved = false;

		logger.info("Initial minimum weighted degree: " + initialObjective);

		debugAndTraceInitialDismabiguationProblemProperties();

		int iterations = 0;
		while (true) {
			iterations++;
			if (iterations == 1) {
				/**
				 * currently, compute the shortest-path distances only in the
				 * first iteration.
				 * */
				removeInitialEntitiesByDistance();

				if (isTracing) {
					traceCleanedGraphStructure();
				}
			}

			int removableMinimumNode = getRemovableMinimumNode();

			if (removableMinimumNode == -1) {
				if (iterations == 1) {
					noMinRemoved = true;
				}
				logger.info("No node can be removed without violating constraints.");
				break;

			}

			double removableMinimumWeightedDegree = entityWeightedDegrees
					.get(removableMinimumNode);

			entitySortedDegrees.remove(removableMinimumNode + ":::"
					+ removableMinimumWeightedDegree);
			entityWeightedDegrees.remove(removableMinimumNode);
			graph.setRemoved(removableMinimumNode);

			updateNeighboringNodes(removableMinimumNode);

			if (isTracing) {
				traceEntityRemovalStep(removableMinimumNode, removableMinimumWeightedDegree);
			}

			double removableMin = Double.POSITIVE_INFINITY;
			double notRemovableMin = Double.POSITIVE_INFINITY;

			if (!entitySortedDegrees.isEmpty()) {
				removableMin = Double.parseDouble(entitySortedDegrees.peek()
						.split(":::")[1]);
			} else {
				logger.info("No node can be removed without violating constraints.");
				break;
			}

			if (!notRemovableSorted.isEmpty()) {
				notRemovableMin = Double.parseDouble(notRemovableSorted.peek()
						.split(":::")[1]);
			}

			double absoluteMinimumWeightedDegree = Math.min(removableMin,
					notRemovableMin);

			double objective = calculateObjective(
					absoluteMinimumWeightedDegree, entityWeightedDegrees);

			if (objective > bestValue) {
				bestValue = objective;
				bestRemovable = new HashSet<Integer>(
						entityWeightedDegrees.keySet());
				bestNotRemovable = new HashSet<Integer>(
						notRemovableEntities.keySet());

				bestMentionDegrees = new HashMap<Integer, Integer>();
				for (int men : mentionDegrees.keySet()) {
					bestMentionDegrees.put(men, mentionDegrees.get(men));
				}

				if (isTracing) {
					// keep track of actual weights
					bestWeightedDegrees = new HashMap<Integer, Double>();
					for (int men : entityWeightedDegrees.keySet()) {
						bestWeightedDegrees.put(men,
								entityWeightedDegrees.get(men));
					}
				}

				for (int b = 0; b < bestRemoved.length; b++) {
					bestRemoved[b] = graph.isRemoved(b);
				}
			}

		} // end main loop of the algorithm

		if (noMinRemoved) {
			double removableMin = Double.POSITIVE_INFINITY;
			double notRemovableMin = Double.POSITIVE_INFINITY;

			if (!entitySortedDegrees.isEmpty())
				removableMin = Double.parseDouble(entitySortedDegrees.peek()
						.split(":::")[1]);
			if (!notRemovableSorted.isEmpty())
				notRemovableMin = Double.parseDouble(notRemovableSorted.peek()
						.split(":::")[1]);
			double absoluteMinimumWeightedDegree = Math.min(removableMin,
					notRemovableMin);

			double objective = calculateObjective(
					absoluteMinimumWeightedDegree, entityWeightedDegrees);

			if (objective >= bestValue) {
				bestValue = objective;
				bestRemovable = new HashSet<Integer>(
						entityWeightedDegrees.keySet());
				bestNotRemovable = new HashSet<Integer>(
						notRemovableEntities.keySet());

				bestMentionDegrees = new HashMap<Integer, Integer>();
				for (int men : mentionDegrees.keySet()) {
					bestMentionDegrees.put(men, mentionDegrees.get(men));
				}

				for (int b = 0; b < bestRemoved.length; b++) {
					bestRemoved[b] = graph.isRemoved(b);
				}
			}
		}

		if (isTracing) {			
			traceFinalGraphStructure();
		}

		logger.debug("Maximizing the minimum weighted degree. Best solution: " + bestValue);

		GraphTracer.gTracer
				.addStat(
						graph.getName(),
						"Solution of Objective after Graph Algorithm (max. min-weighted-degree)",
						Double.toString(bestValue));

		HashSet<Integer> finalEntities = new HashSet<Integer>(bestRemovable);
		finalEntities.addAll(bestNotRemovable);

		double[][] allCloseness = new double[graph.getNodesCount()][graph
				.getNodesCount()];

		for (int m : bestMentionDegrees.keySet()) {
			double[] shortest = shortestPath.closeness(m, graph, bestRemoved);
			for (int e : finalEntities) {
				allCloseness[e][m] = shortest[e];
			}
		}

		int mentions = bestMentionDegrees.keySet().size();
		int entities = finalEntities.size();
		
		debugAndTraceFinalDismabiguationProblemProperties(mentions, entities);

		graph.setIsRemovedFlags(bestRemoved);

		// Graph algorithm is done, check if further disambiguation is needed
		boolean extraDisambiguationNeeded = false;
		for (int mention : bestMentionDegrees.keySet()) {
			int mentionDegree = bestMentionDegrees.get(mention);
			if (mentionDegree > 1) { // more than a candidate entity
				extraDisambiguationNeeded = true;
				break;
			}
		}

		String stat = extraDisambiguationNeeded ? "Need final solving"
				: "Solved after graph";
		GraphTracer.gTracer.addStat(graph.getName(),
				"Status after graph algorithm", stat);

		if (!extraDisambiguationNeeded) {
			logger.debug("No need for further disambiguation");
			long end = System.currentTimeMillis();
			double seconds = ((double) (end - start)) / 1000.0;
			GraphTracer.gTracer.addStat(graph.getName(), "Runtime",
					String.format("%.4fs", seconds));
			fillInSolutionObject(finalEntities, allCloseness);

		} else {
			logger.debug("Applying disambiguation");
			GreedyHillClimbing search = new GreedyHillClimbing(graph,
					bestMentionDegrees.keySet(), finalEntities, bestRemoved,
					10000);
			Map<Integer, Integer> intidSolution = null;

			String solver = useExhaustiveSearch ? "Using exhaustive search"
					: "Using Random Local Search";

			GraphTracer.gTracer.addStat(graph.getName(),
					"Final Solving Technique", solver);

			if (useExhaustiveSearch) {
				intidSolution = search.runExhaustive(graph.getName());
				if (intidSolution == null) {
					intidSolution = search.localSearch(graph.getName(),
							graph.getNodesCount());
				}
			} else {
				intidSolution = search.localSearch(graph.getName(),
						graph.getNodesCount());
			}

			solution = new HashMap<ResultMention, List<ResultEntity>>();
			for (int mentionNodeId : mentionDegrees.keySet()) {
				GraphNode mentionNode = graph.getNode(mentionNodeId);
				Mention mention = (Mention) mentionNode.getNodeData();

				ResultMention rm = new ResultMention(graph.getName(),
						mention.getMention(), mention.getCharOffset(),
						mention.getCharLength());

				if (intidSolution.containsKey(mentionNodeId)
						&& intidSolution.get(mentionNodeId) != -1) {

					int entityNodeId = intidSolution.get(mentionNodeId);
					GraphNode entityNode = graph.getNode(entityNodeId);
					String entity = (String) entityNode.getNodeData();

					/* OLD CONFIDENCE */
					double mentionEntitySimilarity = mentionNode
							.getSuccessors().get(entityNodeId);
					solution.put(rm, ResultEntity
							.getResultEntityAsList(new ResultEntity(entity,
									mentionEntitySimilarity)));
				} else {
					solution.put(rm, ResultEntity
							.getResultEntityAsList(ResultEntity
									.getNoMatchingEntity()));
				}
			}

			long end = System.currentTimeMillis();
			double seconds = ((double) (end - start)) / 1000.0;
			GraphTracer.gTracer.addStat(graph.getName(), "Runtime",
					String.format("%.4fs", seconds));
		}

		return solution;

	}

	protected int getDiameter() throws IOException {
		return 1;
	}

	protected double calculateObjective(double absoluteMinimumWeightedDegree,
			Map<Integer, Double> ewd) {
		if (useNormalizedObjective) {
			return absoluteMinimumWeightedDegree / ewd.size();
		} else {
			return absoluteMinimumWeightedDegree;
		}
	}

	private TIntLinkedList getEntityMentionsNodesIds(int entityNodeId) {
		TIntLinkedList mentions = new TIntLinkedList();

		GraphNode entityNode = graph.getNode(entityNodeId);
		TIntDoubleHashMap successorsMap = entityNode.getSuccessors();
		TIntDoubleIterator successorsIterator = successorsMap.iterator();
		for (int i = successorsMap.size(); i-- > 0;) {
			successorsIterator.advance();

			int successorId = successorsIterator.key();
			GraphNode successorNode = graph.getNode(successorId);
			if (successorNode.getType() == GraphNodeTypes.MENTION) {
				mentions.add(successorId);
			}
		}
		return mentions;
	}

	private Map<String, Double> getConnectedEntities(int nodeId) {
		Map<String, Double> entities = new HashMap<String, Double>();

		GraphNode entityNode = graph.getNode(nodeId);

		TIntDoubleHashMap successorsMap = entityNode.getSuccessors();
		TIntDoubleIterator successorsIterator = successorsMap.iterator();
		for (int i = successorsMap.size(); i-- > 0;) {
			successorsIterator.advance();

			int successorId = successorsIterator.key();
			GraphNode successorNode = graph.getNode(successorId);

			if (successorNode.getType() == GraphNodeTypes.ENTITY) {
				String entity = (String) successorNode.getNodeData();
				String entityName = entity;
				double weight = successorsIterator.value();

				entities.put(entityName, weight);
			}
		}
		return entities;
	}

	private void fillInSolutionObject(HashSet<Integer> finalEntities,
			double[][] allCloseness) {
		for (int mentionNodeId : bestMentionDegrees.keySet()) {
			GraphNode mentionNode = graph.getNode(mentionNodeId);
			Mention mention = (Mention) mentionNode.getNodeData();

			ResultMention rm = new ResultMention(graph.getName(),
					mention.getMention(), mention.getCharOffset(),
					mention.getCharLength());

			int mentionOutdegree = graph.getNodeOutdegree(mentionNodeId);
			if (mentionOutdegree == 0) {
				solution.put(rm, ResultEntity
						.getResultEntityAsList(ResultEntity
								.getNoMatchingEntity()));
			} else {
				TIntDoubleHashMap successorsMap = mentionNode.getSuccessors();
				TIntDoubleIterator successorsIterator = successorsMap
						.iterator();
				for (int i = successorsMap.size(); i-- > 0;) {
					successorsIterator.advance();

					int entityNodeId = successorsIterator.key();
					double mentionEntitySimilarity = successorsIterator.value();
					if (finalEntities.contains(entityNodeId)) {
						double confidence = mentionEntitySimilarity;
						double averageCloseness = 0.0;

						for (int otherMention : bestMentionDegrees.keySet()) {
							if (otherMention == mentionNodeId
									|| allCloseness[entityNodeId][otherMention] == Double.NEGATIVE_INFINITY) {
								continue;
							}
							averageCloseness += allCloseness[entityNodeId][otherMention];
						}

						int numOtherMentions = bestMentionDegrees.keySet()
								.size() - 1;
						if (numOtherMentions > 0) {
							averageCloseness = averageCloseness
									/ numOtherMentions;
						}
						confidence += averageCloseness;

						GraphNode entityNode = graph.getNode(entityNodeId);
						String entity = (String) entityNode.getNodeData();
						List<ResultEntity> res = new ArrayList<ResultEntity>(1);
						res.add(new ResultEntity(entity, confidence));

						solution.put(rm, res);
					}

				}

			}
		}
	}

	private void updateNeighboringNodes(int removableMinimumNodeId) {
		GraphNode node = graph.getNode(removableMinimumNodeId);
		TIntDoubleHashMap successorsMap = node.getSuccessors();
		TIntDoubleIterator successorsIterator = successorsMap.iterator();
		for (int i = successorsMap.size(); i-- > 0;) {
			successorsIterator.advance();

			int successorId = successorsIterator.key();
			double edgeWeight = successorsIterator.value();

			GraphNode successorNode = graph.getNode(successorId);
			if (successorNode.getType() == GraphNodeTypes.MENTION) {
				// successor is a mention node, just update the degree
				int mentionNodeDegree = mentionDegrees.get(successorId);
				mentionDegrees.put(successorId, --mentionNodeDegree);
				if (mentionNodeDegree == 1) {
					// this mention has one remaining candidate
					// Find this remaining candidate
					TIntDoubleHashMap candidatesMap = successorNode.getSuccessors();
					TIntDoubleIterator candidatesIterator = candidatesMap
							.iterator();
					for (int j = candidatesMap.size(); j-- > 0;) {
						candidatesIterator.advance();
						int candidateNodeId = candidatesIterator.key();
						if (!graph.isRemoved(candidateNodeId)) {
							// mark this candidate as non removable if not
							// already marked
							if (entityWeightedDegrees
									.containsKey(candidateNodeId)) {
								double weightedDegree = entityWeightedDegrees
										.get(candidateNodeId);
								entityWeightedDegrees.remove(candidateNodeId);
								entitySortedDegrees.remove(candidateNodeId
										+ ":::" + weightedDegree);

								notRemovableEntities.put(candidateNodeId,
										weightedDegree);
								notRemovableSorted.add(candidateNodeId + ":::"
										+ weightedDegree);
							}
							break;
						}
					}

				}

			} else {
				// successor is an entity. update its weighted degree
				edgeWeight *= entityAlpha;
				if (entityWeightedDegrees.get(successorId) != null) {
					double oldWeightedDegree = entityWeightedDegrees
							.get(successorId);
					double newWeightedDegree = oldWeightedDegree - edgeWeight;
					entityWeightedDegrees.put(successorId, newWeightedDegree);
					entitySortedDegrees.remove(successorId + ":::"
							+ oldWeightedDegree);
					entitySortedDegrees.add(successorId + ":::"
							+ newWeightedDegree);
				} else if (notRemovableEntities.get(successorId) != null) {

					double oldWeightedDegree = notRemovableEntities
							.get(successorId);
					double newWeightedDegree = oldWeightedDegree - edgeWeight;
					notRemovableEntities.put(successorId, newWeightedDegree);
				}
			}
		} // end updating all the neighbor nodes
	}

	private int getRemovableMinimumNode() {
		int removableMinimumNode = -1;

		while (removableMinimumNode == -1 && !entitySortedDegrees.isEmpty()) {
			String minimumEntityString = entitySortedDegrees.peek();
			int minimumEntity = Integer.parseInt(minimumEntityString
					.split(":::")[0]);
			double minimumWeightedDegree = Double
					.parseDouble(minimumEntityString.split(":::")[1]);

			boolean removable = isNodeRemovable(minimumEntity);
			if (!removable) {
				entityWeightedDegrees.remove(minimumEntity);
				entitySortedDegrees.remove(minimumEntity + ":::"
						+ minimumWeightedDegree);
				notRemovableEntities.put(minimumEntity, minimumWeightedDegree);
				notRemovableSorted.add(minimumEntity + ":::"
						+ minimumWeightedDegree);
			} else {
				// Mark the entity as removable
				removableMinimumNode = minimumEntity;
			}
		}
		return removableMinimumNode;
	}

	private boolean isNodeRemovable(int nodeId) {
		GraphNode node = graph.getNode(nodeId);
		if (node.getType() == GraphNodeTypes.MENTION) // this is a mention node
			return false;
		// Check if the entity is removable

		TIntDoubleHashMap successorsMap = node.getSuccessors();
		TIntDoubleIterator successorsIterator = successorsMap.iterator();
		for (int i = successorsMap.size(); i-- > 0;) {
			successorsIterator.advance();

			int successorNodeId = successorsIterator.key();
			GraphNode successorNode = graph.getNode(successorNodeId);
			// if mention and mention connected to only one entity
			if (successorNode.getType() == GraphNodeTypes.MENTION
					&& mentionDegrees.get(successorNodeId) == 1) {
				return false;
			}

		}
		return true;
	}

	protected void removeInitialEntitiesByDistance() {
		ArrayList<Integer> toRemove = new ArrayList<Integer>();

		double[][] allDistances = new double[graph.getNodesCount()][graph
				.getNodesCount()];

		HashMap<Integer, Integer> checkMentionDegree = new HashMap<Integer, Integer>();
		HashMap<Integer, Double> mentionMaxWeightedDegree = new HashMap<Integer, Double>();
		HashMap<Integer, Integer> mentionMaxEntity = new HashMap<Integer, Integer>();

		for (int m : mentionDegrees.keySet()) {
			double[] shortest = shortestPath.run(m, graph);
			for (int e : entityWeightedDegrees.keySet()) {
				allDistances[e][m] = shortest[e];
			}
		} // end distance loop

		for (GraphNode node : graph.getNodes()) {
			int nodeId = node.getId();
			if (graph.isRemoved(nodeId))
				continue;
			// If the node is a mention, skip.
			if (node.getType() == GraphNodeTypes.MENTION) {
				continue;
			}

			double entityDistance = calcEntityDistance(allDistances[nodeId]);

			if (entityDistance > threshold) {
				TIntDoubleHashMap successorsMap = node.getSuccessors();
				TIntDoubleIterator successorsIterator = successorsMap
						.iterator();
				for (int i = successorsMap.size(); i-- > 0;) {
					successorsIterator.advance();

					int successorNodeId = successorsIterator.key();

					if (!graph.isEntityNode(successorNodeId)) {
						if (checkMentionDegree.get(successorNodeId) == null)
							checkMentionDegree.put(successorNodeId, 1);
						else
							checkMentionDegree
									.put(successorNodeId,
											1 + checkMentionDegree
													.get(successorNodeId));
						double weightedDegree = entityWeightedDegrees
								.get(nodeId);
						if (mentionMaxWeightedDegree.get(successorNodeId) == null) {
							mentionMaxWeightedDegree.put(successorNodeId,
									weightedDegree);
							mentionMaxEntity.put(successorNodeId, nodeId);
						} else {
							if (weightedDegree > mentionMaxWeightedDegree
									.get(successorNodeId)) {
								mentionMaxWeightedDegree.put(successorNodeId,
										weightedDegree);
								mentionMaxEntity.put(successorNodeId, nodeId);
							}
						}
					} // end mention neighbor
				}// end scanning neighbors of the entity selected
					// for
					// removal.
				if (!toRemove.contains(nodeId))
					toRemove.add(nodeId);

			}
		}

		removeAndUpdateEntities(toRemove, checkMentionDegree, mentionMaxEntity,
				mentionMaxWeightedDegree);
	}

	protected void removeAndUpdateEntities(List<Integer> toRemove,
			Map<Integer, Integer> checkMentionDegree,
			Map<Integer, Integer> mentionMaxEntity,
			Map<Integer, Double> mentionMaxWeightedDegree) {

		// Filter the list of entities to be removed, saving at least
		// one entity for mention
		for (int mention : checkMentionDegree.keySet()) {

			if (checkMentionDegree.get(mention).intValue() == mentionDegrees
					.get(mention).intValue()) {
				int maxEntity = mentionMaxEntity.get(mention);
				double maxWeightedDegree = mentionMaxWeightedDegree
						.get(mention);
				toRemove.remove(new Integer(maxEntity));
				entityWeightedDegrees.remove(maxEntity);
				entitySortedDegrees.remove(maxEntity + ":::"
						+ maxWeightedDegree);

				notRemovableEntities.put(maxEntity, maxWeightedDegree);
				notRemovableSorted.add(maxEntity + ":::" + maxWeightedDegree);
			}
		}

		for (int en : toRemove) {
			GraphNode node = graph.getNode(en);
			TIntDoubleHashMap successorsMap = node.getSuccessors();
			TIntDoubleIterator successorsIterator = successorsMap.iterator();
			for (int i = successorsMap.size(); i-- > 0;) {
				successorsIterator.advance();

				int successorId = successorsIterator.key();
				double edgeWeight = successorsIterator.value();
				if (graph.isMentionNode(successorId)) {
					// Mention successor
					int oldDegree = mentionDegrees.get(successorId);
					mentionDegrees.put(successorId, --oldDegree);
				} else {
					if (entityWeightedDegrees.get(successorId) != null) {
						edgeWeight *= entityAlpha;
						double oldWeightedDegree = entityWeightedDegrees
								.get(successorId);

						double newWeightedDegree = oldWeightedDegree
								- edgeWeight;
						entityWeightedDegrees.put(successorId,
								newWeightedDegree);
						entitySortedDegrees.remove(successorId + ":::"
								+ oldWeightedDegree);
						entitySortedDegrees.add(successorId + ":::"
								+ newWeightedDegree);
					}

					else if (notRemovableEntities.get(successorId) != null) {
						edgeWeight *= entityAlpha;
						double oldWeightedDegree = notRemovableEntities
								.get(successorId);
						double newWeightedDegree = oldWeightedDegree
								- edgeWeight;
						notRemovableEntities
								.put(successorId, newWeightedDegree);
						notRemovableSorted.remove(successorId + ":::"
								+ oldWeightedDegree);
						notRemovableSorted.add(successorId + ":::"
								+ newWeightedDegree);
					}
				}

			} // end updating all the neighbor nodes

			double oldDegree = entityWeightedDegrees.get(en);
			entitySortedDegrees.remove(en + ":::" + oldDegree);
			entityWeightedDegrees.remove(en);
			graph.setRemoved(en);

			// removed++;
		} // end remove loop
		logger.debug("Iteration 1 Nodes removed: " + toRemove.size());

		GraphTracer.gTracer.addStat(graph.getName(),
				"Entities removed by distance constraint",
				Integer.toString(toRemove.size()));
	}

	protected double calcEntityDistance(double[] ds) {
		ArrayList<Double> finiteDistanceNodes = new ArrayList<Double>();
		double finiteDistance = 0.0;

		for (int w : mentionDegrees.keySet()) {
			if (ds[w] != Double.POSITIVE_INFINITY) {
				finiteDistanceNodes.add(ds[w]);
				finiteDistance += ds[w];
			}
		}

		double entityDistance = Double.NaN;

		if (finiteDistanceNodes.size() > 0) {
			entityDistance = finiteDistance / finiteDistanceNodes.size();
		}

		return entityDistance;
	}

	private double firstScanAndCalculateInitialObjective() throws IOException {
		double initialObjective = Double.POSITIVE_INFINITY;

		for (GraphNode node : graph.getNodes()) {
			int nodeId = node.getId();
			int degree = graph.getNodeOutdegree(nodeId);
			if (graph.isMentionNode(nodeId)) { // mention node
				mentionDegrees.put(nodeId, degree);
				bestMentionDegrees.put(nodeId, degree);
			} else { // entity node
				double weightedDegree = graph.getNodeWeightedDegrees(nodeId);
				boolean notRemovable = false;

				TIntDoubleHashMap successorsMap = node.getSuccessors();
				TIntDoubleIterator successorsIterator = successorsMap
						.iterator();
				for (int i = successorsMap.size(); i-- > 0;) {
					successorsIterator.advance();

					int successorId = successorsIterator.key();

					if (graph.isMentionNode(successorId)) {
						// The current successor is a mention
						if (graph.getNodeOutdegree(successorId) == 1)
							notRemovable = true;
					}
				}

				if (notRemovable) {
					notRemovableEntities.put(nodeId, weightedDegree);
					notRemovableSorted.add(nodeId + ":::" + weightedDegree);
				} else {
					entitySortedDegrees.add(nodeId + ":::" + weightedDegree);
					entityWeightedDegrees.put(nodeId, weightedDegree);
				}
				if (weightedDegree < initialObjective) {
					initialObjective = weightedDegree;
				}
			}
		}

		return initialObjective;

	}

	
	private void debugAndTraceInitialGraphProperties(int diameter,
			double globalAverageWeight) {
		logger.debug("Using " + this.getClass() + " to solve");
		logger.debug("Diameter: " + diameter);
		logger.debug("Average Edge Weight: " + globalAverageWeight);
		logger.debug("Resulting threshold: " + threshold);
		logger.debug("Number of nodes: " + graph.getNodesCount());
		logger.debug("Number of edges: " + graph.getEdgesCount());

		GraphTracer.gTracer.addStat(graph.getName(), "Graph Algorithm", this
				.getClass().getCanonicalName());
		GraphTracer.gTracer.addStat(graph.getName(), "Diameter",
				Integer.toString(diameter));
		GraphTracer.gTracer.addStat(graph.getName(), "Avergage Edge Weight",
				Double.toString(globalAverageWeight));
		GraphTracer.gTracer.addStat(graph.getName(), "Distance Threshold",
				Double.toString(threshold));
		GraphTracer.gTracer.addStat(graph.getName(), "Number of Initial Nodes",
				Integer.toString(graph.getNodesCount()));
		GraphTracer.gTracer.addStat(graph.getName(), "Number of Initial Edges",
				Long.toString(graph.getEdgesCount()));
	}

	private void traceIntitialGraphStructure() {
		for (int menNodeId : mentionDegrees.keySet()) {
			GraphNode menNode = graph.getNode(menNodeId);
			Mention mention = (Mention) menNode.getNodeData();

			TIntDoubleHashMap successorsMap = menNode.getSuccessors();
			TIntDoubleIterator successorsIterator = successorsMap.iterator();
			for (int i = successorsMap.size(); i-- > 0;) {
				successorsIterator.advance();

				int successorNodeId = successorsIterator.key();
				double sim = successorsIterator.value();

				double weight = 0.0;

				if (entityWeightedDegrees.containsKey(successorNodeId)) {
					weight = entityWeightedDegrees.get(successorNodeId);
				} else {
					weight = notRemovableEntities.get(successorNodeId);
				}

				GraphNode entityNode = graph.getNode(successorNodeId);
				String entity = (String) entityNode.getNodeData();

				GraphTracer.gTracer.addCandidateEntityToOriginalGraph(
						graph.getName(), mention.getIdentifiedRepresentation(),
						entity, weight, sim,
						getConnectedEntities(successorNodeId));

			}
		}
	}

	private void debugAndTraceInitialDismabiguationProblemProperties() {
		logger.debug("Initial number of entities: "
				+ entitySortedDegrees.size());
		logger.debug("Initial number of mentions: "
				+ mentionDegrees.keySet().size());

		GraphTracer.gTracer.addStat(graph.getName(),
				"Number of Initial Mentions",
				Integer.toString(mentionDegrees.keySet().size()));
		GraphTracer.gTracer.addStat(graph.getName(),
				"Number of Initial Entities",
				Integer.toString(entitySortedDegrees.size()));

	}

	private void debugAndTraceFinalDismabiguationProblemProperties(
			int mentions, int entities) {

		logger.debug("Number of nodes in the final solution: ");
		logger.debug("Mentions " + mentions);
		logger.debug("Entities " + entities);

		GraphTracer.gTracer.addStat(graph.getName(),
				"Final Number of Mentions", Integer.toString(mentions));
		GraphTracer.gTracer.addStat(graph.getName(),
				"Final Number of Entities", Integer.toString(entities));
	}

	private void traceCleanedGraphStructure() {

		for (int menNodeId : mentionDegrees.keySet()) {

			GraphNode menNode = graph.getNode(menNodeId);
			Mention mention = (Mention) menNode.getNodeData();

			TIntDoubleHashMap successorsMap = menNode.getSuccessors();
			TIntDoubleIterator successorsIterator = successorsMap.iterator();
			for (int i = successorsMap.size(); i-- > 0;) {
				successorsIterator.advance();

				int successorNodeId = successorsIterator.key();

				if (!graph.isRemoved(successorNodeId)) {
					double sim = 0;
					double weight = 0.0;
					if (entityWeightedDegrees.containsKey(successorNodeId)) {
						weight = entityWeightedDegrees.get(successorNodeId);
					} else {
						weight = notRemovableEntities.get(successorNodeId);
					}

					GraphNode entityNode = graph.getNode(successorNodeId);
					String entity = (String) entityNode.getNodeData();

					GraphTracer.gTracer.addCandidateEntityToCleanedGraph(
							graph.getName(),
							mention.getIdentifiedRepresentation(), entity,
							weight, sim);
				}
			}
		}

	}

	private void traceEntityRemovalStep(int removableMinimumNode,
			double removableMinimumWeightedDegree) {
		TIntLinkedList entityMentions = getEntityMentionsNodesIds(removableMinimumNode);
		GraphNode node = graph.getNode(removableMinimumNode);
		String entity = (String) node.getNodeData();
		List<String> entityMentionsStringsIds = new LinkedList<String>();
		TIntIterator iterator = entityMentions.iterator();
		while (iterator.hasNext()) {
			int mentionNodeId = iterator.next();
			GraphNode mentionNode = graph.getNode(mentionNodeId);
			Mention mention = (Mention) mentionNode.getNodeData();
			String mentionIdString = mention.getIdentifiedRepresentation();
			entityMentionsStringsIds.add(mentionIdString);
		}
		GraphTracer.gTracer.addEntityRemovalStep(graph.getName(), entity,
				removableMinimumWeightedDegree, entityMentionsStringsIds);
	}

	private void traceFinalGraphStructure() {
		for (int menNodeId : mentionDegrees.keySet()) {

			GraphNode menNode = graph.getNode(menNodeId);
			Mention mention = (Mention) menNode.getNodeData();

			TIntDoubleHashMap successorsMap = menNode.getSuccessors();
			TIntDoubleIterator successorsIterator = successorsMap.iterator();
			for (int i = successorsMap.size(); i-- > 0;) {
				successorsIterator.advance();

				int successorNodeId = successorsIterator.key();
				if (!bestRemoved[successorNodeId]) {

					double sim = 0;
					double weight = 0.0;

					if (bestWeightedDegrees.containsKey(successorNodeId)) {
						weight = bestWeightedDegrees.get(successorNodeId);
					} else if (notRemovableEntities
							.containsKey(successorNodeId)) {
						weight = notRemovableEntities.get(successorNodeId);
					} else {
						weight = -1; // weight will be taken from the
										// removal steps
					}

					GraphNode entityNode = graph.getNode(successorNodeId);
					String entity = (String) entityNode.getNodeData();

					GraphTracer.gTracer.addCandidateEntityToFinalGraph(
							graph.getName(),
							mention.getIdentifiedRepresentation(), entity,
							weight, sim);
				}

			}
		}
	}
}
