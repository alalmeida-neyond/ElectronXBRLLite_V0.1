package com.example.demo.DTOs;

import java.util.ArrayList;

import org.springframework.data.jpa.domain.Specification;

import com.example.demo.controller.Objects.Entities.Conf.ConfTemplate;

import jakarta.persistence.criteria.Predicate;

public final class ConfTemplateSpecsDTO {
  private ConfTemplateSpecsDTO() {}

  public static Specification<ConfTemplate> filtered(
      Integer templateId,
      String filename,
      String serverFilename,
      String entryPointURL,
      String jsonFileName,
      Integer moduleVid 
  ) {
    return (root, q, cb) -> {
      var ps = new ArrayList<Predicate>();

      if (templateId != null) {
        ps.add(cb.equal(root.get("templateID"), templateId));
      }
      if (filename != null && !filename.isBlank()) {
        ps.add(cb.like(cb.lower(root.get("filename")), "%" + filename.toLowerCase() + "%"));
      }
      if (serverFilename != null && !serverFilename.isBlank()) {
        ps.add(cb.like(cb.lower(root.get("serverFilename")), "%" + serverFilename.toLowerCase() + "%"));
      }
      if (entryPointURL != null && !entryPointURL.isBlank()) {
        ps.add(cb.like(cb.lower(root.get("entryPointURL")), "%" + entryPointURL.toLowerCase() + "%"));
      }
      if (jsonFileName != null && !jsonFileName.isBlank()) {
        ps.add(cb.like(cb.lower(root.get("JSONFileName")), "%" + jsonFileName.toLowerCase() + "%"));
      }
      if (moduleVid != null) {
        ps.add(cb.equal(root.join("moduleVersion").get("moduleVid"), moduleVid));
      }

      return ps.isEmpty() ? cb.conjunction() : cb.and(ps.toArray(Predicate[]::new));
    };
  }
}
