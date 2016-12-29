package br.com.versalius.akijob.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import br.com.versalius.akijob.R;
import br.com.versalius.akijob.network.NetworkHelper;
import br.com.versalius.akijob.network.ResponseCallback;
import br.com.versalius.akijob.utils.ProgressDialogHelper;

public class SingupActivity extends AppCompatActivity implements View.OnFocusChangeListener {

    private TextInputLayout tilName;
    private TextInputLayout tilEmail;
    private TextInputLayout tilPassword;
    private TextInputLayout tilPasswordAgain;
    private TextInputLayout tilPhone;

    private EditText etName;
    private EditText etEmail;
    private EditText etPassword;
    private EditText etPasswordAgain;
    private EditText etBirthday;
    private EditText etPhone;

    private RadioGroup rgGender;
    private RadioButton rbMale;
    private RadioButton rbFemale;

    private ArrayAdapter<String> spCityArrayAdapter;
    private ArrayList<String> spCityListData;
    private HashMap<String,String> cityIdList;

    /* Form data */
    private String cityId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_singup);

        getSupportActionBar().setLogo(R.drawable.toolbar_logo);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.title_signup);

        setUpViews();
    }

    private void setUpViews() {
        /* Instanciando layouts */
        tilName = (TextInputLayout) findViewById(R.id.tilName);
        tilEmail = (TextInputLayout) findViewById(R.id.tilEmail);
        tilPassword = (TextInputLayout) findViewById(R.id.tilPassword);
        tilPasswordAgain = (TextInputLayout) findViewById(R.id.tilPasswordAgain);
        tilPhone = (TextInputLayout) findViewById(R.id.tilPhone);

        /* Instanciando campos */
        etName = (EditText) findViewById(R.id.etName);
        etEmail = (EditText) findViewById(R.id.etEmail);
        etPassword = (EditText) findViewById(R.id.etPassword);
        etPasswordAgain = (EditText) findViewById(R.id.etPasswordAgain);
        etBirthday = (EditText) findViewById(R.id.etBirthday);
        etPhone = (EditText) findViewById(R.id.etPhone);

        /* Adicionando FocusListener*/
        etName.setOnFocusChangeListener(this);
        etEmail.setOnFocusChangeListener(this);
        etPassword.setOnFocusChangeListener(this);
        etPasswordAgain.setOnFocusChangeListener(this);
        etPhone.setOnFocusChangeListener(this);

        /* Adicionando máscara */
        etPhone.addTextChangedListener(new TextWatcher() {
            boolean isErasing = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                /* Se depois da mudança não serão acrescidos caracteres, está apagando */
                isErasing = (after == 0);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String lastChar = "";
                int digits = etPhone.getText().toString().length();
                /* Se não está apagando, verifica se algo precisa ser adicionado */
                if (!isErasing) {
                    if (digits > 0) {
                        lastChar = etPhone.getText().toString().substring(digits - 1);
                    }
                    switch (digits) {
                        case 1:
                            String digit = etPhone.getText().toString();
                            etPhone.setText("");
                            etPhone.append("(" + digit);
                            break;
                        case 3:
                            etPhone.append(")");
                            break;
                        /* Quando o ")" é apagado */
                        case 4:
                            if (!lastChar.equals(")")) {
                                String currentDigits = etPhone.getText().toString().substring(0, digits - 1);
                                etPhone.setText("");
                                etPhone.append(currentDigits + ")" + lastChar);
                            }
                            break;
                        /* Assumindo números no formatp (99)9999-9999*/
                        case 8:
                            etPhone.append("-");
                            break;
                        /* Quando o "-" é apagado */
                        case 9:
                            if (!lastChar.equals("-")) {
                                String currentDigits = etPhone.getText().toString().substring(0, digits - 1);
                                etPhone.setText("");
                                etPhone.append(currentDigits + "-" + lastChar);
                            }
                            break;
                        /* Assumindo números no formatp (99)99999-9999*/
                        case 14:
                            try {
                                String currentDigits[] = etPhone.getText().toString().split("-");
                                if (currentDigits[0].length() == 8) {
                                    currentDigits[1] = new StringBuilder(currentDigits[1]).insert(1, "-").toString();
                                    etPhone.setText("");
                                    etPhone.append(currentDigits[0] + currentDigits[1]);
                                }
                            } catch (Exception e) {
                                //TODO: Lançar exceção
                            }
                            break;
                    }
                } else { /* Se apagou o último dígito deixando o número no formato (99)9999-9999 */
                    if (digits == 13) {
                        try {
                            String currentDigits[] = etPhone.getText().toString().split("-");
                            if (currentDigits[1].length() == 3) {
                                currentDigits[0] = new StringBuilder(currentDigits[0]).insert(currentDigits[0].length() - 1, "-").toString();
                                etPhone.setText("");
                                etPhone.append(currentDigits[0] + currentDigits[1]);
                            }
                        } catch (Exception e) {
                            //TODO: Lançar exceção
                        }
                    }
                }
            }
        });

        /* Radio buttons*/
        rgGender = (RadioGroup) findViewById(R.id.rgGender);
        rbMale = (RadioButton) findViewById(R.id.rbMale);
        rbFemale = (RadioButton) findViewById(R.id.rbFemale);

        /**** Seta o comportamento do DatePicker ****/
        final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MMM/yyyy", Locale.US);
        Calendar nowCalendar = Calendar.getInstance();
        final DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                etBirthday.setText(dateFormatter.format(newDate.getTime()));
            }

        }, nowCalendar.get(Calendar.YEAR), nowCalendar.get(Calendar.MONTH), nowCalendar.get(Calendar.DAY_OF_MONTH));

        etBirthday.setInputType(InputType.TYPE_NULL);
        etBirthday.setText(dateFormatter.format(nowCalendar.getTime()));
        //Abre o Date Picker com click (só funciona se o campo tiver foco)
        etBirthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                datePickerDialog.show();
            }
        });
        //Abre o Date Picker assim que o campo receber foco
        etBirthday.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                    datePickerDialog.show();
            }
        });

        final Spinner spCity = (Spinner) findViewById(R.id.spCity);
        spCity.setEnabled(false);
        spCityListData = new ArrayList<>();
        spCityListData.add("Selecione uma cidade...");
        spCityArrayAdapter = new ArrayAdapter<String>(SingupActivity.this, android.R.layout.simple_spinner_dropdown_item, spCityListData);
        spCity.setAdapter(spCityArrayAdapter);

        final Spinner spState = (Spinner) findViewById(R.id.spState);
        spState.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                /* Carrega o array de ids */
                String[] state_ids = getResources().getStringArray(R.array.array_states_id);
                /* Através da posição do estado selecionado no spinner, descobre-se o id dele */
                int selectedStateId = Integer.valueOf(state_ids[spState.getSelectedItemPosition()]);

                cityIdList = new HashMap<>();
                /* Se o valor do item selecionado é 0, o item selecionado é "Selecione um estado...". Logo, não há seleção válida*/
                if (selectedStateId == 0) {
                    spCityListData.clear();
                    spCityListData.add(getResources().getString(R.string.hint_city_spinner));
                    cityIdList.put(getResources().getString(R.string.hint_city_spinner),"0"); /* O id do primeiro item do spinner é nulo (ou seja, é zero)*/
                    spCity.setEnabled(false);
                    spCityArrayAdapter.notifyDataSetChanged();
                    return;
                }

                final ProgressDialogHelper progressHelper = new ProgressDialogHelper(SingupActivity.this);
                progressHelper.createProgressSpinner("Aguarde", "Atualizando cidades", true, false);

                NetworkHelper.getInstance(SingupActivity.this).getCities(selectedStateId, new ResponseCallback() {
                    @Override
                    public void onSuccess(String jsonStringResponse) {
                        try {
                            spCityListData.clear();
                            spCityListData.add(getResources().getString(R.string.hint_city_spinner));
                            cityIdList.put(getResources().getString(R.string.hint_city_spinner),"0"); /* O id do primeiro item do spinner é nulo (ou seja, é zero)*/
                            JSONArray jArray = new JSONArray(jsonStringResponse);
                            if (jArray != null) {
                                for (int i = 0; i < jArray.length(); i++) {
                                    spCityListData.add(jArray.getJSONObject(i).getString("name"));
                                    cityIdList.put(jArray.getJSONObject(i).getString("name"),jArray.getJSONObject(i).getString("id"));
                                }
                            }
                            spCity.setEnabled(true);
                            spCityArrayAdapter.notifyDataSetChanged();
                            progressHelper.dismiss();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFail(VolleyError error) {
                        Log.i("RESPONSE-FAIL", error.getMessage());
                        progressHelper.dismiss();
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        Button btSingUp = (Button) findViewById(R.id.btSingup);
        btSingUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isValidForm();
            }
        });
    }

    /**
     * Valida os campos do formulário setando mensagens de erro
     */
    private boolean isValidForm() {
        boolean isFocusRequested = false;
        /* Verifica se o campo Nome */
        if (!hasValidName()) {
            tilName.requestFocus();
            isFocusRequested = true;
        }

        /* Verifica o campo de e-mail*/
        if (!hasValidEmail()) {
            if (!isFocusRequested) {
                tilEmail.requestFocus();
                isFocusRequested = true;
            }
        }

        /* Verifica o campo de senha*/
        if (!hasValidPassword()) {
            if (!isFocusRequested) {
                tilPassword.requestFocus();
                isFocusRequested = true;
            }
        }

        /* Verifica o campo de senha repetida*/
        if (!hasValidRepeatedPassword()) {
            if (!isFocusRequested) {
                tilPasswordAgain.requestFocus();
                isFocusRequested = true;
            }
        }

        /* Verifica se os radio buttons estão descelecionados*/
        if (!hasValidGender()) {
            if (!isFocusRequested) {
                rgGender.requestFocus();
                isFocusRequested = true;
            }
        }

        /* Verifica o campo de telefone*/
        if (!hasValidPhone()) {
            if (!isFocusRequested) {
                tilPhone.requestFocus();
                isFocusRequested = true;
            }
        }

        /* Se ninguém pediu foco então tá tudo em ordem */
        return !isFocusRequested;
    }

    private boolean hasValidPhone() {
        String phone = etPhone.getText().toString().trim();
        String phoneNumber[] = phone.split("-");

        if ((TextUtils.isEmpty(phone) ||
                (phone.length() < 13)) ||
                (phoneNumber.length < 2) ||
                (phoneNumber[1].length() != 4) ||
                (phoneNumber[0].length() != 8 &&
                        phoneNumber[0].length() != 9)) {
            tilPhone.setError(getResources().getString(R.string.err_msg_invalid_phone));
            return false;
        }
        tilPhone.setErrorEnabled(false);
        return true;
    }

    private boolean hasValidGender() {
        if (!rbMale.isChecked() && !rbFemale.isChecked()) {
            (findViewById(R.id.tvRgErrMessage)).setVisibility(View.VISIBLE);
            return false;
        }
        (findViewById(R.id.tvRgErrMessage)).setVisibility(View.GONE);
        return true;
    }

    private boolean hasValidRepeatedPassword() {
        String passwordAgain = etPasswordAgain.getText().toString().trim();
        if (!etPassword.getText().toString().trim().equals(passwordAgain)) {
            tilPasswordAgain.setError(getResources().getString(R.string.err_msg_dont_match_password));
            return false;
        }
        tilPasswordAgain.setErrorEnabled(false);
        return true;
    }

    private boolean hasValidPassword() {
        String password = etPassword.getText().toString().trim();
        if (TextUtils.isEmpty(password) || (password.length() < 6) || (password.length() > 22)) {
            tilPassword.setError(getResources().getString(R.string.err_msg_short_password));
            return false;
        }
        if (!TextUtils.isEmpty(etPasswordAgain.getText().toString().trim())) {
            hasValidRepeatedPassword();
        }
        tilPassword.setErrorEnabled(false);
        return true;
    }

    private boolean hasValidEmail() {
        if (TextUtils.isEmpty(etEmail.getText().toString().trim())) {
            tilEmail.setError(getResources().getString(R.string.err_msg_empty_email));
            return false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(etEmail.getText()).matches()) {
            tilEmail.setError(getResources().getString(R.string.err_msg_invalid_email));
            return false;
        }
        tilEmail.setErrorEnabled(false);
        return true;
    }

    private boolean hasValidName() {
        if (TextUtils.isEmpty(etName.getText().toString().trim())) {
            tilName.setError(getResources().getString(R.string.err_msg_empty_name));
            return false;
        }
        tilName.setErrorEnabled(false);
        return true;
    }

    /**
     * NÃO REMOVER DE NOVO!!!!
     * Basicamente seta a ação de fechar a activity ao selecionar a seta na toolbar
     *
     * @param menuItem
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        if (!hasFocus) { /* Verifica somente quando o foco é perdido */
            switch (view.getId()) {
                case R.id.etName:
                    hasValidName();
                    break;
                case R.id.etEmail:
                    hasValidEmail();
                    break;
                case R.id.etPassword:
                    hasValidPassword();
                    break;
                case R.id.etPasswordAgain:
                    hasValidRepeatedPassword();
                    break;
                case R.id.etPhone:
                    hasValidPhone();
                    break;
            }
        }
    }

}
