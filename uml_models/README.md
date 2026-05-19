# UML Models

This directory contains the four UML class diagrams for the CHMS project.

## Files

| Subsystem | Source | Image |
| --- | --- | --- |
| Alert Generation System | `AlertGenerationSystem.puml` | `AlertGenerationSystem.png` |
| Data Storage System | `DataStorageSystem.puml` | `DataStorageSystem.png` |
| Patient Identification System | `PatientIdentificationSystem.puml` | `PatientIdentificationSystem.png` |
| Data Access Layer | `DataAccessLayer.puml` | `DataAccessLayer.png` |

The `.puml` files are the **source of truth**. If a diagram is edited, regenerate
the `.png` with PlantUML:

```sh
plantuml AlertGenerationSystem.puml
```

Or paste the contents into <https://www.plantuml.com/plantuml/>.

## Week 2 Feedback Fixes

The diagrams were updated to address the Week 2 instructor feedback. Specifically:

* **Straight arrows / no crossings** – every diagram now sets
  `skinparam linetype ortho`, which forces orthogonal (right-angle) routing of
  relationship lines.
* **Type of `staffList` in `AlertManager`** – previously typed as
  `List<String>`, now correctly typed as `List<MedicalStaff>`.
* **Connections to Patient Identification & Data Storage in `AlertGenerator`**
  – the Alert Generation diagram now explicitly shows `AlertGenerator` reading
  from `DataStorage` and resolving simulator IDs through `PatientIdentifier`.
