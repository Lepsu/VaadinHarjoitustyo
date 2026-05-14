package com.example.examplefeature.ui;

import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.component.charts.model.DataSeriesItem;
import com.vaadin.flow.component.charts.model.ListSeries;
import com.vaadin.flow.component.charts.model.Marker;
import com.vaadin.flow.component.charts.model.PlotOptionsAreaspline;
import com.vaadin.flow.component.charts.model.PointPlacement;
import com.vaadin.flow.component.charts.model.XAxis;
import com.vaadin.flow.component.dashboard.Dashboard;
import com.vaadin.flow.component.dashboard.DashboardSection;
import com.vaadin.flow.component.dashboard.DashboardWidget;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.dom.Style;
import jakarta.annotation.security.RolesAllowed;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.Menu;

@PageTitle("Admin")
@RolesAllowed({})
@Route(value = "admin")
@Menu(title = "Admin")
public class Admin extends Main {

    public Admin() {
        Dashboard board = new Dashboard();
        board.setMaximumColumnCount(4);
        board.setMinimumColumnWidth("200px");
        board.setMinimumRowHeight("150px");
        DashboardSection highlights = board.addSection("Highlights");
        highlights.add(createHighlight("Current users", "745", 33.7));
        highlights.add(createHighlight("View events", "54.6k", -112.45));
        highlights.add(createHighlight("Conversion rate", "18%", 3.9));
        highlights.add(createHighlight("Custom metric", "-123.45", 0.0));
        DashboardSection details = board.addSection("Details");
        details.add(createViewEvents());
        details.add(createServiceHealth());
        details.add(createResponseTimes());
        add(board);
    }

    private DashboardWidget createHighlight(String title, String value, Double percentage) {
        VaadinIcon icon = VaadinIcon.ARROW_UP;
        String prefix = "";
        String theme = "badge";
        if (percentage == 0) {
            prefix = "±";
        } else if (percentage > 0) {
            prefix = "+";
            theme += " success";
        } else if (percentage < 0) {
            icon = VaadinIcon.ARROW_DOWN;
            theme += " error";
        }
        Icon i = icon.create();
        i.getStyle().setPadding("var(--vaadin-padding-xs)");
        Span span = new Span(value);
        span.getStyle().setFontWeight(Style.FontWeight.BOLD);
        span.getStyle().setFontSize("1.5rem");
        Span percentageSpan = new Span(prefix + percentage);
        Span badge = new Span(i, percentageSpan);
        badge.getElement().getThemeList().add(theme);
        badge.getStyle().setPadding("var(--vaadin-padding-m)");
        VerticalLayout layout = new VerticalLayout(span, badge);
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        layout.setSpacing(false);
        DashboardWidget widget = new DashboardWidget();
        widget.setTitle(title);
        widget.setContent(layout);
        return widget;
    }

    private DashboardWidget createViewEvents() {
        // Header
        Select year = new Select();
        year.setItems("2011", "2012", "2013", "2014", "2015", "2016", "2017", "2018", "2019", "2020", "2021");
        year.setValue("2021");
        year.setWidth("100px");
        // Chart
        Chart chart = new Chart(ChartType.AREASPLINE);
        Configuration conf = chart.getConfiguration();
        conf.getChart().setStyledMode(true);
        XAxis xAxis = new XAxis();
        xAxis.setCategories("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec");
        conf.addxAxis(xAxis);
        conf.getyAxis().setTitle("Values");
        PlotOptionsAreaspline plotOptions = new PlotOptionsAreaspline();
        plotOptions.setPointPlacement(PointPlacement.ON);
        plotOptions.setMarker(new Marker(false));
        conf.addPlotOptions(plotOptions);
        conf.addSeries(new ListSeries("Berlin", 189, 191, 291, 396, 501, 403, 609, 712, 729, 942, 1044, 1247));
        conf.addSeries(new ListSeries("London", 138, 246, 248, 348, 352, 353, 463, 573, 778, 779, 885, 887));
        conf.addSeries(new ListSeries("New York", 65, 65, 166, 171, 293, 302, 308, 317, 427, 429, 535, 636));
        conf.addSeries(new ListSeries("Tokyo", 0, 11, 17, 123, 130, 142, 248, 349, 452, 454, 458, 462));
        HorizontalLayout headerContent = new HorizontalLayout(new Span("City/month"), year);
        headerContent.setAlignItems(FlexComponent.Alignment.CENTER);
        // Add it all together
        DashboardWidget widget = new DashboardWidget();
        widget.setTitle("View events");
        widget.setHeaderContent(headerContent);
        widget.setContent(chart);
        widget.setColspan(4);
        widget.setRowspan(2);
        return widget;
    }

    private DashboardWidget createServiceHealth() {
        // Grid
        Grid<ServiceHealth> grid = new Grid();
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.setAllRowsVisible(true);
        grid.addColumn(new ComponentRenderer<>(serviceHealth -> {
            Span status = new Span();
            String statusText = getStatusDisplayName(serviceHealth);
            status.getElement().setAttribute("aria-label", "Status: " + statusText);
            status.getElement().setAttribute("title", "Status: " + statusText);
            status.getElement().getThemeList().add(getStatusTheme(serviceHealth));
            return status;
        })).setHeader("").setFlexGrow(0).setAutoWidth(true);
        grid.addColumn(ServiceHealth::getCity).setHeader("City").setFlexGrow(1);
        grid.addColumn(ServiceHealth::getInput).setHeader("Input").setAutoWidth(true).setTextAlign(ColumnTextAlign.END);
        grid.addColumn(ServiceHealth::getOutput).setHeader("Output").setAutoWidth(true).setTextAlign(ColumnTextAlign.END);
        grid.setItems(new ServiceHealth(Status.EXCELLENT, "Münster", 324, 1540), new ServiceHealth(Status.OK, "Cluj-Napoca", 311, 1320), new ServiceHealth(Status.FAILING, "Ciudad Victoria", 300, 1219));
        // Add it all together
        DashboardWidget widget = new DashboardWidget();
        widget.setTitle("Service health");
        widget.setHeaderContent(new Span("Input / output"));
        widget.setContent(grid);
        widget.setColspan(2);
        return widget;
    }

    private DashboardWidget createResponseTimes() {
        // Chart
        Chart chart = new Chart(ChartType.PIE);
        Configuration conf = chart.getConfiguration();
        conf.getChart().setStyledMode(true);
        chart.setThemeName("gradient");
        DataSeries series = new DataSeries();
        series.add(new DataSeriesItem("System 1", 12.5));
        series.add(new DataSeriesItem("System 2", 12.5));
        series.add(new DataSeriesItem("System 3", 12.5));
        series.add(new DataSeriesItem("System 4", 12.5));
        series.add(new DataSeriesItem("System 5", 12.5));
        series.add(new DataSeriesItem("System 6", 12.5));
        conf.addSeries(series);
        // Add it all together
        DashboardWidget widget = new DashboardWidget();
        widget.setTitle("Response times");
        widget.setHeaderContent(new Span("Avarage across all systems"));
        widget.setContent(chart);
        widget.setColspan(2);
        return widget;
    }

    private String getStatusDisplayName(ServiceHealth serviceHealth) {
        Status status = serviceHealth.getStatus();
        if (status == Status.OK) {
            return "Ok";
        } else if (status == Status.FAILING) {
            return "Failing";
        } else if (status == Status.EXCELLENT) {
            return "Excellent";
        } else {
            return status.toString();
        }
    }

    private String getStatusTheme(ServiceHealth serviceHealth) {
        Status status = serviceHealth.getStatus();
        String theme = "badge primary small";
        if (status == Status.EXCELLENT) {
            theme += " success";
        } else if (status == Status.FAILING) {
            theme += " error";
        }
        return theme;
    }

    private static class ServiceHealth {

        private Status status;

        private String city;

        private Integer input;

        private Integer output;

        public ServiceHealth(Status status, String city, int input, int output) {
            this.status = status;
            this.city = city;
            this.input = input;
            this.output = output;
        }

        public Status getStatus() {
            return status;
        }

        public void setStatus(Status status) {
            this.status = status;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public Integer getInput() {
            return input;
        }

        public void setInput(Integer input) {
            this.input = input;
        }

        public Integer getOutput() {
            return output;
        }

        public void setOutput(Integer output) {
            this.output = output;
        }
    }

    private enum Status {

        FAILING, OK, EXCELLENT
    }
}
