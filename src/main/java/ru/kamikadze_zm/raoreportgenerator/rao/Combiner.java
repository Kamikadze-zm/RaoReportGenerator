package ru.kamikadze_zm.raoreportgenerator.rao;

import ru.kamikadze_zm.raoreportgenerator.MoviesInfoExcel;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import ru.kamikadze_zm.raoreportgenerator.ExcelException;
import ru.kamikadze_zm.raoreportgenerator.MainApp;
import ru.kamikadze_zm.raoreportgenerator.MovieInfo;
import ru.kamikadze_zm.raoreportgenerator.playreports.ExcelPlayReports;
import ru.kamikadze_zm.raoreportgenerator.playreports.PlayReportMovie;

public class Combiner {

    public static void combine(File moviesInfoFile, File playReportsFile) throws ExcelException {
        List<MovieInfo> moviesInfo = MoviesInfoExcel.parse(moviesInfoFile);
        List<PlayReportMovie> playReportMovies = ExcelPlayReports.parse(playReportsFile);
        combine(moviesInfo, playReportMovies);
    }

    public static void combine(List<MovieInfo> moviesInfo, List<PlayReportMovie> playReportMovies) throws ExcelException {
        List<MovieInfo> combinedInfo = new ArrayList<>();

        String replaceRegEx = "[ _\\-.]";
        final String message = "Не найден в сетке СТП";
        MovieInfo founded;
        for (PlayReportMovie prm : playReportMovies) {
            String reportMovieName = prm.getMovieName().replaceAll(replaceRegEx, "").toLowerCase();
            String stpFilmName;
            founded = null;
            for (MovieInfo mi : moviesInfo) {
                stpFilmName = mi.getName().replaceAll(replaceRegEx, "").toLowerCase();
                if (reportMovieName.startsWith(stpFilmName)) {
                    founded = mi;
                    break;
                }
            }
            String[] releases = prm.getDateTime().split(",");
            for (String date : releases) {
                if (founded != null) {
                    String name = prm.getMovieName();
                    if (founded.getOriginalName() != null && !founded.getOriginalName().isEmpty()) {
                        name = name + " (" + founded.getOriginalName() + ")";
                    }
                    combinedInfo.add(getCombinedMovie(founded, prm, name, date));
                } else {
                    combinedInfo.add(new MovieInfo(prm.getMovieName(), date, prm.getDuration().toString(), message));
                }
            }
        }
        MoviesInfoExcel.save(combinedInfo, MainApp.SETTINGS.getRaoPath());
    }

    public static void combineFilms(List<MovieInfo> moviesInfo, File playReportsFile) throws ExcelException {
        List<PlayReportMovie> playReportMovies = ExcelPlayReports.parse(playReportsFile);
        combine(moviesInfo, playReportMovies);
    }

    public static void combinePlayReports(File moviesInfoFile, List<PlayReportMovie> playReportMovies) throws ExcelException {
        List<MovieInfo> moviesInfo = MoviesInfoExcel.parse(moviesInfoFile);
        combine(moviesInfo, playReportMovies);
    }

    private static MovieInfo getCombinedMovie(MovieInfo mi, PlayReportMovie prm, String name, String dateTime) {
        MovieInfo cmi = new MovieInfo(
                name,
                mi.getGenre(),
                mi.getCountry(),
                mi.getYear(),
                mi.getDirector(),
                mi.getComposer(),
                mi.getStudio(),
                prm.getDuration().toString(),
                dateTime,
                mi.getNotFound(),
                mi.getLink());
        cmi.setStpName(mi.getName());
        return cmi;
    }
}