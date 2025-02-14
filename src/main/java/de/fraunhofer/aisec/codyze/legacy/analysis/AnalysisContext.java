
package de.fraunhofer.aisec.codyze.legacy.analysis;

import de.fraunhofer.aisec.cpg.graph.Graph;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.File;
import java.util.*;

public class AnalysisContext {

	/** List of violations of MARK rules. the region, etc. */
	@NonNull
	private final Set<Finding> findings = new HashSet<>();

	private final List<File> sourceLocations;
	private final Graph graph;

	public AnalysisContext(List<File> sourceLocations, Graph graph) {
		this.sourceLocations = sourceLocations;
		this.graph = graph;
	}

	public AnalysisContext(File f, Graph graph) {
		this(List.of(f), graph);
	}

	/**
	 * Returns a (possibly empty) mutable list of findings, i.e. violations of MARK rules that were
	 * found during analysis. Make sure to call {@code analyze()} before as otherwise this method will
	 * return an empty list.
	 *
	 * @return Set of all findings
	 */
	public @NonNull Set<Finding> getFindings() {
		return this.findings;
	}

	public List<File> getSourceLocations() {
		return sourceLocations;
	}

	public Graph getGraph() {
		return this.graph;
	}
}
