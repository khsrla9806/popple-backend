package com.popple.server.domain.survey;

import com.popple.server.domain.entity.SurveyOption;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SurveyOptionRepository extends JpaRepository<SurveyOption, Integer> {
}
