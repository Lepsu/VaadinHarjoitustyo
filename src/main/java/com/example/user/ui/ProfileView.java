package com.example.examplefeature.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import java.time.LocalDate;
import jakarta.annotation.security.PermitAll;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.Menu;

@PageTitle("Profile")
@PermitAll
@Route(value = "profile")
@Menu(title = "Profile")
public class Profile extends Main {

    private final VerticalLayout layout = new VerticalLayout();

    private final TextField firstName = new TextField("First name");

    private final TextField lastName = new TextField("Last name");

    private final EmailField email = new EmailField("Email address");

    private final DatePicker dateOfBirth = new DatePicker("Birthday");

    private final PhoneNumberField phone = new PhoneNumberField("Phone number");

    private final TextField occupation = new TextField("Occupation");

    private final Button cancel = new Button("Cancel");

    private final Button save = new Button("Save");

    private final Binder<SamplePerson> binder = new Binder<>(SamplePerson.class);

    public Profile() {
        layout.add(createTitle());
        layout.add(createFormLayout());
        layout.add(createButtonLayout());
        clearForm();
        cancel.addClickListener(e -> clearForm());
        save.addClickListener(e -> {
            BinderValidationStatus<SamplePerson> validate = binder.validate();
            if (validate.hasErrors()) {
                return;
            }
            Notification.show(binder.getBean().getClass().getSimpleName() + " details stored.");
            clearForm();
        });
        bind();
        binder.setBean(new SamplePerson());
        add(layout);
    }

    private void bind() {
        binder.forField(firstName).asRequired().bind(SamplePerson::getFirstName, SamplePerson::setFirstName);
        binder.forField(lastName).asRequired().bind(SamplePerson::getLastName, SamplePerson::setLastName);
        binder.forField(email).asRequired().bind(SamplePerson::getEmail, SamplePerson::setEmail);
        binder.forField(dateOfBirth).bind(samplePerson -> {
            if (samplePerson.getDateOfBirth() == null) {
                return null;
            }
            return LocalDate.parse(samplePerson.dateOfBirth);
        }, (samplePerson, date) -> samplePerson.setDateOfBirth(date.toString()));
        binder.forField(phone).bind(SamplePerson::getPhone, SamplePerson::setPhone);
        binder.forField(occupation).bind(SamplePerson::getOccupation, SamplePerson::setOccupation);
    }

    private void clearForm() {
        binder.setBean(new SamplePerson());
    }

    private Component createTitle() {
        return new H3("Personal information");
    }

    private Component createFormLayout() {
        FormLayout formLayout = new FormLayout();
        email.setErrorMessage("Please enter a valid email address");
        formLayout.add(firstName, lastName, dateOfBirth, phone, email, occupation);
        return formLayout;
    }

    private Component createButtonLayout() {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.addClassName("button-layout");
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buttonLayout.add(save);
        buttonLayout.add(cancel);
        return buttonLayout;
    }

    private static class PhoneNumberField extends CustomField<String> {

        private final ComboBox<String> countryCode = new ComboBox<>();

        private final TextField number = new TextField();

        public PhoneNumberField(String label) {
            setLabel(label);
            countryCode.setWidth("120px");
            countryCode.setPlaceholder("Country");
            countryCode.setAllowedCharPattern("[\\+\\d]");
            countryCode.setItems("+354", "+91", "+62", "+98", "+964", "+353", "+44", "+972", "+39", "+225");
            countryCode.addCustomValueSetListener(e -> countryCode.setValue(e.getDetail()));
            number.setAllowedCharPattern("\\d");
            HorizontalLayout layout = new HorizontalLayout(countryCode, number);
            layout.setFlexGrow(1.0, number);
            add(layout);
        }

        @Override
        protected String generateModelValue() {
            if (countryCode.getValue() != null && number.getValue() != null) {
                String s = countryCode.getValue() + " " + number.getValue();
                return s;
            }
            return "";
        }

        @Override
        protected void setPresentationValue(String phoneNumber) {
            String[] parts = phoneNumber != null ? phoneNumber.split(" ", 2) : new String[0];
            if (parts.length == 1) {
                countryCode.clear();
                number.setValue(parts[0]);
            } else if (parts.length == 2) {
                countryCode.setValue(parts[0]);
                number.setValue(parts[1]);
            } else {
                countryCode.clear();
                number.clear();
            }
        }
    }

    public static class SamplePerson {

        private String firstName;

        private String lastName;

        private String email;

        private String dateOfBirth;

        private String phone;

        private String occupation;

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getDateOfBirth() {
            return dateOfBirth;
        }

        public void setDateOfBirth(String dateOfBirth) {
            this.dateOfBirth = dateOfBirth;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getOccupation() {
            return occupation;
        }

        public void setOccupation(String occupation) {
            this.occupation = occupation;
        }
    }
}
