
package de.fraunhofer.aisec.codyze.legacy.analysis.markevaluation;

import de.breakpointsec.pushdown.IllegalTransitionException;
import de.fraunhofer.aisec.codyze.legacy.analysis.*;
import de.fraunhofer.aisec.codyze.legacy.analysis.resolution.ConstantValue;
import de.fraunhofer.aisec.codyze.legacy.analysis.wpds.TypestateAnalysis;
import de.fraunhofer.aisec.cpg.graph.Graph;
import de.fraunhofer.aisec.cpg.helpers.Benchmark;
import de.fraunhofer.aisec.mark.markDsl.OrderExpression;
import de.fraunhofer.aisec.codyze.legacy.markmodel.MRule;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrderEvaluator {

	private static final Logger log = LoggerFactory.getLogger(OrderEvaluator.class);
	private final MRule rule;
	private final ServerConfiguration config;

	public OrderEvaluator(@NonNull MRule rule, ServerConfiguration config) {
		this.rule = rule;
		this.config = config;
	}

	public ConstantValue evaluate(OrderExpression orderExpression, Integer contextID, AnalysisContext resultCtx, Graph graph, MarkContextHolder markContextHolder) {
		Benchmark tsBench = new Benchmark(TypestateAnalysis.class, "Typestate Analysis");

		ConstantValue result = null;

		switch (config.typestateAnalysis) {
			case WPDS:
				log.info("Evaluating order with WPDS");
				var ts = new TypestateAnalysis(markContextHolder);
				var context = markContextHolder.getContext(contextID);

				try {
					// NOTE: rule and orderExpression might be redundant as arguments
					result = ts.analyze(orderExpression, context, resultCtx, graph, rule);
				}
				catch (IllegalTransitionException e) {
					log.error("Unexpected error in typestate WPDS", e);
					result = ErrorValue.newErrorValue(String.format("Unexpected error in typestate WPDS %s", e.getMessage()));
				}
				break;

			case DFA:
				log.info("Evaluating order with DFA");
				result = new OrderEvaluation().evaluateDFA(rule, markContextHolder, orderExpression, contextID, resultCtx);
				break;

			default:
				result = ErrorValue.newErrorValue("Unknown typestateanalysis");
		}

		tsBench.stop();
		return result;
	}
}
