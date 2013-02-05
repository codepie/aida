package mpi.aida.graph;

import gnu.trove.map.hash.TIntDoubleHashMap;

public class GraphNode {
	
	private int id;
	private GraphNodeTypes type;
	private Object NodeData = null;
	private TIntDoubleHashMap successors;
	
	public GraphNode() {
		successors = new TIntDoubleHashMap();
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public GraphNodeTypes getType() {
		return type;
	}
	public void setType(GraphNodeTypes type) {
		this.type = type;
	}
	public Object getNodeData() {
		return NodeData;
	}
	public void setNodeData(Object nodeData) {
		NodeData = nodeData;
	}
	public TIntDoubleHashMap getSuccessors() {
		return successors;
	}
	public void setSuccessors(TIntDoubleHashMap successors) {
		this.successors = successors;
	}
	
}
