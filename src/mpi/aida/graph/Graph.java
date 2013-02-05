/**
 * 
 */
package mpi.aida.graph;

import gnu.trove.map.hash.TObjectIntHashMap;

import java.util.Arrays;

import mpi.aida.data.Mention;

/**
 * @author Mohamed Amir Yosef
 *
 */

public class Graph {
	private String name;
	protected int nodesCount;
	private double alpha;
	
	private int edgesCount = 0;
	
	private int nextNodeId = 0;

	
	private GraphNode[] nodes;
	private TObjectIntHashMap<Mention> mentionNodesIds;
	private TObjectIntHashMap<String> entityNodesIds;
	
	protected double averageMEweight = 1.0;
	protected double averageEEweight = 1.0;
	
	protected boolean[] isRemoved;
	protected int[] nodesOutdegrees;
	protected double[] nodesWeightedDegrees;
	
	
	public Graph(String name, int nodesCount, double alpha) {
		this.name = name;
		this.nodesCount = nodesCount;
		this.alpha = alpha;
		
		nodes = new GraphNode[nodesCount];
		mentionNodesIds = new TObjectIntHashMap<Mention>();
		entityNodesIds = new TObjectIntHashMap<String>();

		isRemoved = new boolean[nodesCount];
		Arrays.fill(isRemoved, false);
		nodesOutdegrees = new int[nodesCount];
		Arrays.fill(nodesOutdegrees, 0);
		nodesWeightedDegrees = new double[nodesCount];
		Arrays.fill(nodesWeightedDegrees, 0);
	}
	
	public int getNodeOutdegree(int id) {
		return nodesOutdegrees[id];
	}
	
	public double getNodeWeightedDegrees(int id) {
		return nodesWeightedDegrees[id];
	}
	
	public boolean isRemoved(int id) {
		return isRemoved[id];
	}
	
	public void setRemoved(int id) {
		isRemoved[id] = true;
	}
	
	public void setIsRemovedFlags(boolean[] isRemoved) {
		this.isRemoved = isRemoved;
	}
	
	
	public void addEdge(Mention mention, String entity, double sim) {
		int id1 = mentionNodesIds.get(mention);
		int id2 = entityNodesIds.get(entity);
		addEdge(id1, id2, sim);
	}
	
	public void addEdge(String entity1, String entity2, double coh) {
		int id1 = entityNodesIds.get(entity1);
		int id2 = entityNodesIds.get(entity2);
		addEdge(id1, id2, coh);
	}
	
	
	public void addMentionNode(Mention mention) {
		GraphNode node = new GraphNode();
		node.setId(nextNodeId);
		node.setType(GraphNodeTypes.MENTION);
		node.setNodeData(mention);
		mentionNodesIds.put(mention, nextNodeId);
		nodes[nextNodeId] = node;
		
		nextNodeId++;
	}
	
	public void addEntityNode(String entity) {
		GraphNode node = new GraphNode();
		node.setId(nextNodeId);
		node.setType(GraphNodeTypes.ENTITY);
		node.setNodeData(entity);
		entityNodesIds.put(entity, nextNodeId);
		nodes[nextNodeId] = node;
		
		nextNodeId++;
	}
	
	private void addEdge(int node1Id, int node2Id, double weight) {
		if(isEntityNode(node1Id) && isEntityNode(node2Id))
			weight = weight * (1-alpha);
		else if ((isMentionNode(node1Id) && isEntityNode(node2Id))
				|| (isEntityNode(node1Id) && isMentionNode(node2Id)))
			weight = weight * alpha;
				
		edgesCount++;
		
		GraphNode node1 = nodes[node1Id];
		GraphNode node2 = nodes[node2Id];
		
		node1.getSuccessors().put(node2Id, weight);
		nodesOutdegrees[node1Id]++;
		
		node2.getSuccessors().put(node1Id, weight);
		nodesOutdegrees[node2Id]++;
		
		nodesWeightedDegrees[node1Id] += weight;
		nodesWeightedDegrees[node2Id] += weight;		
	}
	
	public boolean isEntityNode(int nodeId) {
		if(nodes[nodeId].getType() == GraphNodeTypes.ENTITY)
			return true;
		else
			return false;
	}
	
	public boolean isMentionNode(int nodeId) {
		if(nodes[nodeId].getType() == GraphNodeTypes.MENTION)
			return true;
		else
			return false;
	}
	
	public int getNodesCount() {
		return nodesCount;
	}

	public long getEdgesCount() {
		return edgesCount;
	}
	
	public GraphNode[] getNodes() {
		return nodes;
	}
	
	public GraphNode getNode(int id) {
		return nodes[id];
	}

	
	public double getAverageMEweight() {
		return averageMEweight;
	}

	public void setAverageMEweight(double averageMEweight) {
		this.averageMEweight = averageMEweight;
	}

	public double getAverageEEweight() {
		return averageEEweight;
	}

	public void setAverageEEweight(double averageEEweight) {
		this.averageEEweight = averageEEweight;
	}


	public String getName() {
		return name;
	}

	public TObjectIntHashMap<Mention> getMentionNodesIds() {
		return mentionNodesIds;
	}
}
