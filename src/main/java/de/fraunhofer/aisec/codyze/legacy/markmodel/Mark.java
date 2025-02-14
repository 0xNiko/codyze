
package de.fraunhofer.aisec.codyze.legacy.markmodel;

import de.fraunhofer.aisec.mark.markDsl.EntityDeclaration;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.*;

public class Mark {

	@NonNull
	private final Map<String, MEntity> entityByName = new HashMap<>();

	@NonNull
	private final List<MRule> rules = new ArrayList<>();

	public void addEntities(String name, MEntity ent) {
		this.entityByName.put(name, ent);
	}

	public Collection<MEntity> getEntities() {
		return this.entityByName.values();
	}

	@Nullable
	public MEntity getEntity(EntityDeclaration e) {
		if (e == null) {
			return null;
		}
		return getEntity(e.getName());
	}

	public MEntity getEntity(@NonNull String name) {
		return entityByName.get(name);
	}

	@NonNull
	public List<MRule> getRules() {
		return this.rules;
	}

	public void reset() {
		// nothing to do for the rules
		for (MEntity entity : getEntities()) {
			for (MOp op : entity.getOps()) {
				op.reset();
			}
		}
	}

}
