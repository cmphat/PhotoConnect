package com.photoconnect.service;

import com.photoconnect.dto.PhotographerEvaluationDto;
import com.photoconnect.entity.PhotographerProfile;

/**
 * Service providing rule-based screening assistance for photographer applications.
 * Scores profile completeness and produces objective checklist recommendations
 * without replacing human Manager judgment.
 */
public interface PhotographerEvaluationService {

    /**
     * Evaluates a photographer profile against the 6 standardized screening criteria.
     *
     * @param profile the profile under review
     * @return objective evaluation breakdown, satisfied/missing items, and recommendation
     */
    PhotographerEvaluationDto evaluate(PhotographerProfile profile);
}
