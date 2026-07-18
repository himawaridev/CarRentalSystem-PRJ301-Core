(function() {
    const provinceSelect = document.getElementById('pickupProvince');
    const districtSelect = document.getElementById('pickupDistrict');
    const wardSelect = document.getElementById('pickupWard');
    const specificAddressInput = document.getElementById('pickupSpecificAddress');
    const pickupLocationInput = document.getElementById('pickupLocation');

    const fallbackLocations = [
        { name: 'TP Ho Chi Minh', districts: [
            { name: 'Quan 1', wards: ['Ben Nghe', 'Ben Thanh', 'Da Kao', 'Nguyen Thai Binh'] },
            { name: 'Quan 3', wards: ['Vo Thi Sau', 'Phuong 9', 'Phuong 11'] },
            { name: 'Quan Binh Thanh', wards: ['Phuong 1', 'Phuong 3', 'Phuong 15', 'Phuong 22'] }
        ] },
        { name: 'Ha Noi', districts: [
            { name: 'Quan Hoan Kiem', wards: ['Hang Bac', 'Hang Bong', 'Trang Tien'] },
            { name: 'Quan Ba Dinh', wards: ['Dien Bien', 'Kim Ma', 'Ngoc Ha'] },
            { name: 'Quan Cau Giay', wards: ['Dich Vong', 'Mai Dich', 'Yen Hoa'] }
        ] },
        { name: 'Da Nang', districts: [
            { name: 'Quan Hai Chau', wards: ['Hai Chau I', 'Hai Chau II', 'Thach Thang'] },
            { name: 'Quan Son Tra', wards: ['An Hai Bac', 'An Hai Dong', 'Man Thai'] }
        ] },
        { name: 'Khac', districts: [{ name: 'Khac', wards: ['Khac'] }] }
    ];

    function selectedValue(select) {
        return select ? select.getAttribute('data-selected') || '' : '';
    }

    function normalizeLocations(data) {
        return (data || []).map(function(province) {
            return {
                name: province.name,
                districts: (province.districts || []).map(function(district) {
                    return {
                        name: district.name,
                        wards: (district.wards || []).map(function(ward) { return ward.name; })
                    };
                })
            };
        }).filter(function(province) { return province.name; });
    }

    function ensureOther(locations) {
        if (!locations.some(function(item) { return item.name === 'Khac'; })) {
            locations.push({ name: 'Khac', districts: [{ name: 'Khac', wards: ['Khac'] }] });
        }
        return locations;
    }

    function setOptions(select, values, placeholder, selected) {
        select.innerHTML = '';
        const empty = document.createElement('option');
        empty.value = '';
        empty.textContent = placeholder;
        select.appendChild(empty);
        values.forEach(function(value) {
            const option = document.createElement('option');
            option.value = value;
            option.textContent = value;
            if (value === selected) option.selected = true;
            select.appendChild(option);
        });
    }

    function findByName(items, name) {
        return (items || []).find(function(item) { return item.name === name; });
    }

    function updatePickupLocationValue() {
        if (!pickupLocationInput) return;
        const parts = [
            specificAddressInput ? specificAddressInput.value.trim() : '',
            wardSelect ? wardSelect.value : '',
            districtSelect ? districtSelect.value : '',
            provinceSelect ? provinceSelect.value : ''
        ].filter(Boolean);
        pickupLocationInput.value = parts.join(', ');
    }

    function initLocationSelectors(locations) {
        locations = ensureOther(locations && locations.length ? locations : fallbackLocations);
        const initialProvince = selectedValue(provinceSelect);
        const initialDistrict = selectedValue(districtSelect);
        const initialWard = selectedValue(wardSelect);

        setOptions(provinceSelect, locations.map(function(item) { return item.name; }), 'Tinh/TP', initialProvince);

        function refreshDistricts(selectedDistrict) {
            const province = findByName(locations, provinceSelect.value);
            const districts = province ? province.districts : [];
            setOptions(districtSelect, districts.map(function(item) { return item.name; }), 'Quan/Huyen', selectedDistrict || '');
            refreshWards(initialWard);
        }

        function refreshWards(selectedWard) {
            const province = findByName(locations, provinceSelect.value);
            const district = province ? findByName(province.districts, districtSelect.value) : null;
            setOptions(wardSelect, district ? district.wards : [], 'Phuong/Xa', selectedWard || '');
            updatePickupLocationValue();
        }

        provinceSelect.addEventListener('change', function() {
            provinceSelect.setAttribute('data-selected', provinceSelect.value);
            districtSelect.setAttribute('data-selected', '');
            wardSelect.setAttribute('data-selected', '');
            refreshDistricts('');
        });
        districtSelect.addEventListener('change', function() {
            districtSelect.setAttribute('data-selected', districtSelect.value);
            wardSelect.setAttribute('data-selected', '');
            refreshWards('');
        });
        wardSelect.addEventListener('change', updatePickupLocationValue);
        if (specificAddressInput) specificAddressInput.addEventListener('input', updatePickupLocationValue);

        refreshDistricts(initialDistrict);
        refreshWards(initialWard);
    }

    if (provinceSelect && districtSelect && wardSelect) {
        fetch('https://provinces.open-api.vn/api/?depth=3')
            .then(function(response) {
                if (!response.ok) throw new Error('Cannot load province data');
                return response.json();
            })
            .then(function(data) { initLocationSelectors(normalizeLocations(data)); })
            .catch(function() { initLocationSelectors(fallbackLocations); });
    }

    const summary = document.getElementById('bookingSummary');
    const driverFeeText = document.getElementById('summaryDriverFee');
    const driverCheckboxes = Array.from(document.querySelectorAll('.booking-driver-checkbox'));

    function parseAmount(value) {
        const number = Number.parseFloat(value || '0');
        return Number.isFinite(number) ? number : 0;
    }

    function formatVnd(value) {
        return new Intl.NumberFormat('vi-VN', { maximumFractionDigits: 0 }).format(value) + ' VND';
    }

    function updateBookingSummary() {
        if (!summary) return;
        const driverFeePerCar = parseAmount(summary.dataset.driverFeePerCar);
        const selectedDriverCount = driverCheckboxes.filter(function(checkbox) { return checkbox.checked; }).length;
        const driverFee = selectedDriverCount * driverFeePerCar;

        if (driverFeeText) driverFeeText.textContent = formatVnd(driverFee);
    }

    driverCheckboxes.forEach(function(checkbox) {
        checkbox.addEventListener('change', updateBookingSummary);
    });
    updateBookingSummary();
})();
