package com.trego.service;


import com.trego.dto.view.SubstituteDetailView;



import java.util.List;

public interface ISubstituteService {
    List<SubstituteDetailView> findSubstitute(Long id);

}
