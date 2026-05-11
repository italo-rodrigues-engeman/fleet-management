package com.indux.modules.cdi.aplication.service;

import com.indux.modules.cdi.aplication.dtos.ApplicantDTO;
import com.indux.modules.cdi.aplication.dtos.AvaliationDTO;
import com.indux.modules.cdi.aplication.dtos.PointsDTO;
import com.indux.modules.cdi.domain.entities.models.*;
import com.indux.modules.cdi.domain.entities.mongo.CdiEntity;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class CDIFixtures {

    public static List<PointsDTO> fakeGetAllPoints(){
        return Arrays.asList(
            new PointsDTO("1", 30, 20, 10),
            new PointsDTO("2", 30, 20, 10),
            new PointsDTO("3", 30, 20, 10),
            new PointsDTO("4", 30, 20, 10)
        );
    }

    public static CdiEntity fakeCreateCdi(Stage stage){
        CdiEntity cdi = new CdiEntity(
                "teste",
                1,// id
                new ApplicantDTO("teste","teste","teste","teste","teste","teste","teste","teste","tetse","teste"),    // applicant
                Type.DESENVOLVIMENTO,                 // type (substitua Type.TIPO_1 com o tipo desejado)
                Scope.CONTRATO,               // scope (substitua Scope.SCOPE_1 com o valor desejado)
                Complexity.ALTO,        // complexity (substitua Complexity.COMPLEX_1 com o valor desejado)
                PrevistTime.CURTO,          // previstTime (substitua PrevistTime.TIME_1 com o valor desejado)
                "teste",                     // description
                false,                        // independent
                null,     // details
                "teste",                     // problem
                "teste",                     // result
                null,  // especificResult
                Status.INTERROMPIDO,             // status (substitua Status.STATUS_1 com o valor desejado)
                stage,               // stage (substitua Stage.STAGE_1 com o valor desejado)
                null,  // local
                null,  // dir1
                null,  // dir2
                new Date(),                  // createAt
                70,                         // points
                null,        // priortyLevel
                null,   // finalResponsabilty
                1,
                null,
                "titulo"
        );
        return cdi;
    }

    public static CdiEntity fakeUpdateLocalCDI(Stage stage){
        CdiEntity cdi = new CdiEntity(
                "teste",
                1,// id
                new ApplicantDTO("teste","teste","teste","teste","teste","teste","teste","teste","tetse","teste"),    // applicant
                Type.DESENVOLVIMENTO,                 // type (substitua Type.TIPO_1 com o tipo desejado)
                Scope.CONTRATO,               // scope (substitua Scope.SCOPE_1 com o valor desejado)
                Complexity.ALTO,        // complexity (substitua Complexity.COMPLEX_1 com o valor desejado)
                PrevistTime.CURTO,          // previstTime (substitua PrevistTime.TIME_1 com o valor desejado)
                "teste",                     // description
                false,                        // independent
                null,     // details
                "teste",                     // problem
                "teste",                     // result
                null,  // especificResult
                Status.INTERROMPIDO,             // status (substitua Status.STATUS_1 com o valor desejado)
                stage,               // stage (substitua Stage.STAGE_1 com o valor desejado)
                new AvaliationDTO(10,10,10,"teste",new Date(),null,null),  // local
                null,  // dir1
                null,  // dir2
                new Date(),                  // createAt
                110,                         // points
                null,        // priortyLevel
                null,   // finalResponsabilty
                1,
                null,
                "titulo"
        );
        return cdi;
    }

    public static CdiEntity fakeUpdateDir2ComiteCDI(Status status){
        CdiEntity cdi = new CdiEntity(
                "teste",                     // id
                1,
                new ApplicantDTO("teste","teste","teste","teste","teste","teste","teste","teste","tetse","teste"),    // applicant
                Type.DESENVOLVIMENTO,                 // type (substitua Type.TIPO_1 com o tipo desejado)
                Scope.CONTRATO,               // scope (substitua Scope.SCOPE_1 com o valor desejado)
                Complexity.ALTO,        // complexity (substitua Complexity.COMPLEX_1 com o valor desejado)
                PrevistTime.CURTO,          // previstTime (substitua PrevistTime.TIME_1 com o valor desejado)
                "teste",                     // description
                false,                        // independent
                null,     // details
                "teste",                     // problem
                "teste",                     // result
                null,  // especificResult
                status,             // status (substitua Status.STATUS_1 com o valor desejado)
                Stage.AÇÃO,               // stage (substitua Stage.STAGE_1 com o valor desejado)
                new AvaliationDTO(10,10,10,"teste",new Date(), null, null),  // local
                null,  // dir1
                null,  // dir2
                new Date(),                  // createAt
                110,                         // points
                null,        // priortyLevel
                null,   // finalResponsabilty
                1,
                null,
                "titulo"
        );
        return cdi;
    }

}
