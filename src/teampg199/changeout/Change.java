package teampg199.changeout;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Change data consumed by {@link PageChangeBroadcaster}.
 *
 * @author JWill <Jackson.Williams at camosun.ca>
 */
public abstract class Change<T> {
	protected final T affected;

	protected Change(T affected) {
		this.affected = affected;
	}

	public T getAffected() {
		return affected;
	}

	/**
	 * Returns true if changes act on the same thing.
	 */
	@Override
	public abstract boolean equals(Object other);

	/**
	 * Given a newer Change, allows colliding attributes to be overwritten.
	 *
	 * @param newer
	 *            Newer Change of same type
	 */
	public abstract Change<T> merge(Change<T> newer);

	/**
	 * Two changes that act on the same thing should have the same hashCode.
	 *
	 * e.g. Two BoardChanges acting on x:3,y:2 or Two EntityChanges acting on
	 * the same Entity
	 */
	@Override
	public abstract int hashCode();

	// TODO TESTME
	public static <C extends Change> void mergeAll(Collection<C> newChangesToAdd,
			Set<C> oldChangesToOverwrite) {
		List<C> toMerge = new LinkedList<>();

		// add all non-colliding changes
		for (C ch : newChangesToAdd) {
			if (oldChangesToOverwrite.contains(ch)) {
				toMerge.add(ch);
				continue;
			}

			oldChangesToOverwrite.add(ch);
		}

		// merge colliding changes
		// loop through all old changes, and see if it's one we need to merge
		List<C> mergedChangesToAdd = new LinkedList<>();
		for (Iterator<C> i = oldChangesToOverwrite.iterator(); i.hasNext();) {
			if (toMerge.isEmpty()) {
				break;
			}

			C olderChange = i.next();

			if (!toMerge.contains(olderChange)) {
				continue;
			}

			// get merged change
			int newerToMergeIndex = toMerge.indexOf(olderChange);
			C newerChange = toMerge.get(newerToMergeIndex);
			C mergedChange = (C) olderChange.merge(newerChange);

			// remove old and new changes
			i.remove();
			toMerge.remove(newerToMergeIndex);

			// if two changes nullified each other, don't add null
			if (mergedChange == null) {
				continue;
			}

			// add merged
			mergedChangesToAdd.add(mergedChange);
		}

		oldChangesToOverwrite.addAll(mergedChangesToAdd);
	}
}
