/*
 * Copyright [2016] [George Papadakis (gpapadis@yahoo.gr)]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package DataReader.GroundTruthReader;

import DataModel.EntityProfile;
import DataModel.IdDuplicates;
import DataReader.AbstractReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

/**
 *
 * @author G.A.P. II
 */

public abstract class AbstractGtReader extends AbstractReader implements IGroundTruthReader {
    
    protected int datasetLimit;
    protected int noOfEntities;

    protected final Set<IdDuplicates> idDuplicates;
    protected final Map<String, Integer> urlToEntityId1;
    protected final Map<String, Integer> urlToEntityId2;
    protected final SimpleGraph duplicatesGraph;
    
    public AbstractGtReader (String filePath) {
        super(filePath);
        idDuplicates = new HashSet<>();
        duplicatesGraph = new SimpleGraph(DefaultEdge.class);
        urlToEntityId1 = new HashMap<>();
        urlToEntityId2 = new HashMap<>();
    }
    
    @Override
    public Set<IdDuplicates> getDuplicatePairs(List<EntityProfile> profiles) {
        return this.getDuplicatePairs(profiles, null);
    }
}