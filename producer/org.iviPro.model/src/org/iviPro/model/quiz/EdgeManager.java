package org.iviPro.model.quiz;

import java.util.Iterator;
import java.util.LinkedList;

import org.iviPro.model.BeanList;
import org.iviPro.model.IQuizBean;
import org.iviPro.model.IQuizContainerBean;
import org.iviPro.model.Project;

public class EdgeManager extends IQuizContainerBean {

	private static final long serialVersionUID = 1L;

	private static EdgeManager INSTANCE;

	private BeanList<Edge> edgeList;

	private EdgeManager(Project project) {
		super(project);
	}

	public static void createInstance(Project project) {
		if(INSTANCE == null) {
			INSTANCE = new EdgeManager(project);
		}
	}

	public static EdgeManager getInstance() {
		return INSTANCE;
	}

	public LinkedList<Integer> getConditionIdsByNodeId(int nodeId) {
		LinkedList<Integer> lst = new LinkedList<Integer>();
		for (Iterator<Edge> iterator = edgeList.iterator(); iterator.hasNext();) {
			Edge ed = (Edge) iterator.next();
			if (ed.getIdNodeDestination() == nodeId) {
				lst.add(ed.getIdCondition());
			}
		}
		return lst;
	}

	public void setEdgeData(Edge edge) {
		boolean isAvailable = false;
		for (Iterator<Edge> iterator = edgeList.iterator(); iterator.hasNext();) {
			Edge ed = (Edge) iterator.next();
			if (ed.getIdNodeSource() == edge.getIdNodeSource()
					&& ed.getIdNodeDestination() == edge.getIdNodeDestination()) {
				isAvailable = true;
			}
		}
		if (!isAvailable) {
			edgeList.add(edge);
		} else {
			for (Iterator<Edge> iterator = edgeList.iterator(); iterator
					.hasNext();) {
				Edge ed = (Edge) iterator.next();
				if (ed.getIdNodeSource() == edge.getIdNodeSource()
						&& ed.getIdNodeDestination() == edge
								.getIdNodeDestination()) {
					ed.setDestination(edge.getDestination());
					ed.setIdCondition(edge.getIdCondition());
				}
			}
		}
	}

	public void deleteKante(int idNodeSource, int idCondition) {
		for (Iterator<Edge> iterator = edgeList.iterator(); iterator.hasNext();) {
			Edge ed = (Edge) iterator.next();
			if (ed.getIdNodeSource() == idNodeSource
					&& ed.getIdCondition() == idCondition) {
				iterator.remove();
			}
		}
	}

	public void deleteEdge(Edge currentEdge) {
		for (Iterator<Edge> iterator = edgeList.iterator(); iterator.hasNext();) {
			Edge ed = (Edge) iterator.next();
			if (ed.getIdNodeSource() == currentEdge.getIdNodeSource()
					&& ed.getIdNodeDestination() == currentEdge
							.getIdNodeDestination()) {
				iterator.remove();
			}
		}
	}

	public int getConditionId(int sourceNodeId, int destNodeId) {
		for (Iterator<Edge> iterator = edgeList.iterator(); iterator.hasNext();) {
			Edge ed = (Edge) iterator.next();
			if (ed.getIdNodeSource() == sourceNodeId
					&& ed.getIdNodeDestination() == destNodeId) {
				return ed.getIdCondition();
			}
		}
		return -1;
	}

	public void updateEdgeCondition(Condition newCondition, int idSource,
			int idDestination, int idBedingung) {
		for (Iterator<Edge> iterator = edgeList.iterator(); iterator.hasNext();) {
			Edge ed = (Edge) iterator.next();
			if (ed.getIdNodeSource() == idSource
					&& ed.getIdNodeDestination() == idDestination) {
				ed.setIdCondition(idBedingung);
			}
		}
	}

	public LinkedList<Edge> getEdgesByNodeId(int idNode) {
		LinkedList<Edge> lst = new LinkedList<Edge>();
		for (Iterator<Edge> iterator = edgeList.iterator(); iterator.hasNext();) {
			Edge ed = (Edge) iterator.next();
			if (ed.getIdNodeSource() == idNode
					|| ed.getIdNodeDestination() == idNode) {
				lst.add(ed);
			}
		}
		return lst;
	}
	
	public Edge getEdgeBySourceAndTarget(int source, int target) {
		for (Iterator<Edge> iterator = edgeList.iterator(); iterator.hasNext();) {
			Edge edge = (Edge) iterator.next();
			if(edge.getIdNodeSource() == source && edge.getIdNodeDestination() == target) {
				return edge;
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setBeanList(BeanList<? extends IQuizBean> list) {
    	edgeList = (BeanList<Edge>)list;	
	}
}
