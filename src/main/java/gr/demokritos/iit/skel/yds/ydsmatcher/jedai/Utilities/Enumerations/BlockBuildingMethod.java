/*
* Copyright [2016-2017] [George Papadakis (gpapadis@yahoo.gr)]
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

package Utilities.Enumerations;

import BlockBuilding.AttributeClusteringBlocking;
import BlockBuilding.ExtendedQGramsBlocking;
import BlockBuilding.ExtendedSortedNeighborhoodBlocking;
import BlockBuilding.ExtendedSuffixArraysBlocking;
import BlockBuilding.IBlockBuilding;
import BlockBuilding.QGramsBlocking;
import BlockBuilding.SortedNeighborhoodBlocking;
import BlockBuilding.StandardBlocking;
import BlockBuilding.SuffixArraysBlocking;
import BlockProcessing.BlockRefinement.BlockFiltering;
import BlockProcessing.ComparisonRefinement.ComparisonPropagation;
import BlockProcessing.ComparisonRefinement.WeightedEdgePruning;
import BlockProcessing.IBlockProcessing;

/**
 *
 * @author G.A.P. II
 */
public enum BlockBuildingMethod {

    ATTRIBUTE_CLUSTERING,
    EXTENDED_Q_GRAMS_BLOCKING,
    EXTENDED_SORTED_NEIGHBORHOOD,
    EXTENDED_SUFFIX_ARRAYS,
    Q_GRAMS_BLOCKING,
    SORTED_NEIGHBORHOOD,
    SUFFIX_ARRAYS,
    STANDARD_BLOCKING;
    
    public static IBlockBuilding getDefaultConfiguration(BlockBuildingMethod blbuMethod) {
        switch (blbuMethod) {
            case ATTRIBUTE_CLUSTERING:
                return new AttributeClusteringBlocking();
            case EXTENDED_Q_GRAMS_BLOCKING:
                return new ExtendedQGramsBlocking();
            case EXTENDED_SORTED_NEIGHBORHOOD:
                return new ExtendedSortedNeighborhoodBlocking();
            case EXTENDED_SUFFIX_ARRAYS:
                return new ExtendedSuffixArraysBlocking();
            case Q_GRAMS_BLOCKING:
                return new QGramsBlocking();
            case SORTED_NEIGHBORHOOD:
                return new SortedNeighborhoodBlocking();
            case SUFFIX_ARRAYS:
                return new SuffixArraysBlocking();
            case STANDARD_BLOCKING:
            default:
                return new StandardBlocking();
        }
    }
    
    public static IBlockProcessing getDefaultBlockCleaning(BlockBuildingMethod blbuMethod) {
        switch (blbuMethod) {
            case ATTRIBUTE_CLUSTERING:
                return new BlockFiltering(0.50);
            case EXTENDED_Q_GRAMS_BLOCKING:
                return new BlockFiltering(0.50);
            case EXTENDED_SORTED_NEIGHBORHOOD:
                return new BlockFiltering(0.45);
            case EXTENDED_SUFFIX_ARRAYS:
                return null;
            case Q_GRAMS_BLOCKING:
                return new BlockFiltering(0.50);
            case SORTED_NEIGHBORHOOD:
                return null;
            case SUFFIX_ARRAYS:
                return null;
            case STANDARD_BLOCKING:
            default:
                return new BlockFiltering(0.55);
        }
    }
    
    public static IBlockProcessing getDefaultComparisonCleaning(BlockBuildingMethod blbuMethod) {
        switch (blbuMethod) {
            case ATTRIBUTE_CLUSTERING:
                return new WeightedEdgePruning(WeightingScheme.CBS);
            case EXTENDED_Q_GRAMS_BLOCKING:
                return new WeightedEdgePruning(WeightingScheme.EJS);
            case EXTENDED_SORTED_NEIGHBORHOOD:
                return new WeightedEdgePruning(WeightingScheme.JS);
            case EXTENDED_SUFFIX_ARRAYS:
                return new ComparisonPropagation();
            case Q_GRAMS_BLOCKING:
                return new WeightedEdgePruning(WeightingScheme.ECBS);
            case SORTED_NEIGHBORHOOD:
                return new ComparisonPropagation();
            case SUFFIX_ARRAYS:
                return new ComparisonPropagation();
            case STANDARD_BLOCKING:
            default:
                return new WeightedEdgePruning(WeightingScheme.CBS);
        }
    }
}
