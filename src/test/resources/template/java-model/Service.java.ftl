
<#assign paramObj = usecase.parameterizedObject>
class Service {

  ${java.nameVariable(usecase.getName())}(${java.nameType(paramObj.name?substring(1))}Request request) throws ServiceException {

  }

}