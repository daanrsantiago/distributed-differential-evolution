function [populationStatisticsData] = getPopulationStatistics(populationId)

request = matlab.net.http.RequestMessage('GET');
[response, completedResponse, ~] = send(request, 'localhost:8080/population/'+string(populationId)+'/statistics');

if(completedResponse.Completed)
    populationStatisticsData = response.Body.Data;
end

end