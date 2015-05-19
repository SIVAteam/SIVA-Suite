package org.iviPro.scenedetection.sd_algorithm;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.iviPro.scenedetection.sd_graph.GraphContent;
import org.iviPro.scenedetection.sd_graph.TemporalGraph;
import org.iviPro.scenedetection.sd_main.Scene;
import org.iviPro.scenedetection.sd_main.Shot;

public class SceneExtraction {

	private int sceneKey;

	private TemporalGraph<Cluster> graph;

	private List<Cluster> shortestPath;

	public SceneExtraction(TemporalGraph<Cluster> graph,
			List<Cluster> shortestPath) {
		this.sceneKey = 0;
		this.graph = graph;
		this.shortestPath = shortestPath;
		graph.markShortestPath(shortestPath);
		// Graph coloring!
		colorGraph();
	}

	public List<Scene> generateScenes() {
		List<Scene> sceneList = new LinkedList<Scene>();
		int start = 0;
		System.out.println("ShortestPathSize: "+shortestPath.size());
		if (shortestPath.size() == 1) {
			sceneList.add(generateSceneObject(getClusterScene(
					shortestPath.get(0), false)));
		} else {
			for (int i = 1; i < shortestPath.size(); i++) {
				setInvisible(i - 1);
				boolean check = graph.checkConnection(shortestPath.get(i - 1),
						shortestPath.get(i));
				if (!check) {
					sceneList.add(generateSceneObject(getClusterScene(
							shortestPath.get(start), false)));
					start = i;
					if (i == shortestPath.size() - 1) {
						sceneList.add(generateSceneObject(getClusterScene(
								shortestPath.get(start), false)));
					}
				} else {
					setVisible(i - 1);
					if (i == shortestPath.size() - 1) {
						sceneList.add(generateSceneObject(getClusterScene(
								shortestPath.get(start), false)));
					}
				}
				graph.unmarkAll();
			}
		}
		return sceneList;
	}

	private List<GraphContent<Cluster>> getClusterScene(Cluster start,
			boolean coloring) {
		return graph.getAllObjectsInCircle(start, coloring);
	}

	private Scene generateSceneObject(List<GraphContent<Cluster>> lst) {
		Scene scene = new Scene(sceneKey);
		for (Iterator<GraphContent<Cluster>> iterator = lst.iterator(); iterator
				.hasNext();) {
			GraphContent<Cluster> graphContent = (GraphContent<Cluster>) iterator
					.next();
			List<Shot> clusterShots = graphContent.getContent().getShotList();
			for (Iterator<Shot> iterator2 = clusterShots.iterator(); iterator2
					.hasNext();) {
				Shot shot = (Shot) iterator2.next();
				scene.addShots(shot, graphContent.isAmbiguous());
			}
		}
		scene.setSettings();
		sceneKey++;
		return scene;
	}

	private void setVisible(int position) {
		graph.changeVisibility(shortestPath.get(position),
				shortestPath.get(position + 1), true);
	}

	private void setInvisible(int position) {
		graph.changeVisibility(shortestPath.get(position),
				shortestPath.get(position + 1), false);
	}

	private void colorGraph() {
		int start = 0;
		for (int i = 1; i < shortestPath.size(); i++) {
			setInvisible(i - 1);
			boolean check = graph.checkConnection(shortestPath.get(i - 1),
					shortestPath.get(i));
			if (!check) {
				getClusterScene(shortestPath.get(start), true);
				start = i;
				if (i == shortestPath.size() - 1) {
					getClusterScene(shortestPath.get(start), true);
				}
				setVisible(i - 1);
			} else {
				setVisible(i - 1);
				if (i == shortestPath.size() - 1) {
					getClusterScene(shortestPath.get(start), true);
				}
			}
			graph.unmarkAll();
		}
	}
}
